package gst.engine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JarTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import gst.api.ActionSpec;
import gst.api.ImportMods;
import gst.api.MappingLoader;
import gst.api.Match;
import gst.api.Recipe;
import gst.api.Step;
import gst.engine.actions.Action;
import gst.engine.actions.ActionFactory;
import gst.engine.matcher.MatchResult;
import gst.engine.matcher.NodeMatcher;
import gst.engine.validator.ValidationError;
import gst.engine.validator.ValidationFactory;
import gst.engine.validator.ValidationRule;

public class Pipeline {

    private static List<String> performParseCheck(String code) {
        List<String> issues = new ArrayList<>();
        
        try {
            StaticJavaParser.parse(code);
        } catch (com.github.javaparser.ParseProblemException e) {
            issues.add("Parse error: " + e.getMessage());
        } catch (Exception e) {
            issues.add("Unexpected parsing issue: " + e.getMessage());
        }
        
        return issues;
    }
    private static List<String> performParseCheck(CompilationUnit cu) {return performParseCheck(cu.toString());}

    public static void run(Path mappingFile, Path inputRoot, Path outputRoot) throws IOException {
        run(mappingFile, inputRoot, outputRoot, List.of(), false);
    }
    
    public static void run(Path mappingFile, Path inputRoot, Path outputRoot, List<Path> jarPaths) throws IOException {
        run(mappingFile, inputRoot, outputRoot, jarPaths, false);
    }

    @SuppressWarnings("UseSpecificCatch")
    public static void run(Path mappingFile, Path inputRoot, Path outputRoot, List<Path> jarPaths, boolean matchDebug) throws IOException {
        List<Recipe> recipes = MappingLoader.load(mappingFile);

        CombinedTypeSolver typeSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                new JavaParserTypeSolver(inputRoot.toFile())
        );

        for (Path jar : jarPaths) {
            try {
                typeSolver.add(new JarTypeSolver(jar.toFile()));
                System.out.println("[INFO] Added JAR to symbol solver: " + jar);
            } catch (Exception e) {
                System.err.println("[WARNING] Could not load JAR for symbol solving: " + jar + " – " + e.getMessage());
            }
        }

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration cfg = new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_21).setSymbolResolver(symbolSolver);
        StaticJavaParser.setConfiguration(cfg);

        TxContext ctx = new TxContext();

        try (Stream<Path> files = Files.walk(inputRoot)) {
            List<Path> javaFiles = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());

            for (Path srcFile : javaFiles) {
                String rel = inputRoot.relativize(srcFile).toString();
                System.out.println("[PROCESS] Processing file: " + rel);
                
                try {
                    String originalContent = Files.readString(srcFile, StandardCharsets.UTF_8);
                    List<String> initialParseIssues = performParseCheck(originalContent);
                    if (!initialParseIssues.isEmpty()) {
                        System.out.println("[INITIAL-PARSE-CHECK] Issues detected in original input file: " + rel);
                        initialParseIssues.forEach(issue -> System.out.println("  [WARNING] " + issue));
                        System.out.println("[INFO] These are pre-existing issues in the source code, not caused by transformations");
                    } else {
                        System.out.println("[INITIAL-PARSE-CHECK] [OK] Original input file has no parsing issues: " + rel);
                    }
                } catch (IOException e) {
                    System.err.println("[ERROR] Could not read file for initial parse check: " + srcFile + " - " + e.getMessage());
                }
                
                CompilationUnit cu;
                try {
                    cu = StaticJavaParser.parse(srcFile);
                } catch (IOException ex) {
                    System.err.println("[ERROR] Failed to parse: " + srcFile + " - " + ex.getMessage());
                    continue;
                }

                boolean fileChanged = false;
                ctx.saveOriginalFile(srcFile, cu); // Save original before any changes

                for (Recipe recipe : recipes) {
                    boolean matchedRecipe = false;
                    System.out.println("[INFO] Applying recipe: " + recipe.name);

                    for (Step step : recipe.steps) {
                        Match m = step.match;
                        List<Node> candidates = NodeMatcher.findCandidates(cu, m.nodeType);

                        for (Node node : candidates) {
                            MatchResult result = NodeMatcher.matches(node, step.match, typeSolver);
                            if (result.matched()) {
                                System.out.println("[MATCH] "
                                        + step.match.nodeType + " at "
                                        + node.getRange().orElse(null));

                                for (ActionSpec spec : step.actions) {
                                    String actionName = spec.getKey();
                                    Map<String, Object> params = spec.getParams();

                                    ctx.saveOriginalNodeForRecipe(recipe.name, node, node.clone());
                                    Action act = ActionFactory.create(actionName, params);
                                    System.out.println("[ACTION] " + actionName
                                            + " on node at " + node.getRange().orElse(null));
                                    act.apply(node, cu, ctx, symbolSolver);
                                    ctx.registerRecipeChange(recipe.name, node);
                                }

                                matchedRecipe = true;
                                fileChanged = true;
                            } else {
                                if (matchDebug) {
                                    List<String> reasons = result.getFailureReasons();
                                    String primaryReason = reasons.isEmpty() ? "unknown" : reasons.get(0);
                                    System.out.println("[MATCH-FAILED] " + step.match.nodeType + " → " + primaryReason);
                                }
                            }
                        }

                    }

                    if (matchedRecipe && recipe.imports != null) {
                        ImportMods im = recipe.imports;
                        if (im.remove != null) {
                            cu.getImports().removeIf(id -> im.remove.contains(id.getNameAsString()));
                            fileChanged = true;
                            System.out.println("[IMPORT] Removed imports: " + im.remove);
                        }
                        if (im.add != null) {
                            for (String imp : im.add) {
                                boolean present = cu.getImports()
                                        .stream()
                                        .anyMatch(i -> i.getNameAsString().equals(imp));
                                if (!present) {
                                    cu.addImport(imp);
                                    fileChanged = true;
                                    System.out.println("[IMPORT] Added import: " + imp);
                                }
                            }
                        }
                    }

                    if (matchedRecipe) {
                        // Perform immediate parse check after recipe application
                        List<String> parseIssues = performParseCheck(cu);
                        if (!parseIssues.isEmpty()) {
                            System.out.println("[PARSE-CHECK] Issues detected after applying recipe '" + recipe.name + "' in file: " + rel);
                            parseIssues.forEach(issue -> System.out.println("  [WARNING] " + issue));
                        } else {
                            System.out.println("[PARSE-CHECK] [OK] No parsing issues detected after recipe '" + recipe.name + "'");
                        }
                        
                        // Run validation per-recipe if this specific recipe has rollbackOnError specified
                        if (recipe.rollbackOnError != null && !recipe.rollbackOnError.trim().isEmpty()) {
                            try {
                                ValidationRule validator = ValidationFactory.create(recipe.rollbackOnError);
                                List<Node> allChangedNodes = ctx.getRecipeChanges(recipe.name);
                                
                                // Filter changed nodes to only include nodes from the current file
                                String currentFilePath = cu.getStorage().map(s -> s.getPath().toString()).orElse(null);
                                List<Node> fileChangedNodes = allChangedNodes.stream()
                                    .filter(node -> {
                                        String nodeFilePath = node.findCompilationUnit()
                                            .flatMap(nodeCu -> nodeCu.getStorage())
                                            .map(s -> s.getPath().toString())
                                            .orElse(null);
                                        return currentFilePath != null && currentFilePath.equals(nodeFilePath);
                                    })
                                    .collect(java.util.stream.Collectors.toList());
                                
                                List<ValidationError> errors = validator.validateRecipeChanges(cu, fileChangedNodes, ctx, symbolSolver, recipe.name);
                                
                                if (!errors.isEmpty()) {
                                    System.out.println("[VALIDATION] Errors found in recipe '" + recipe.name + "' using validator '" + recipe.rollbackOnError + "' in file: " + rel);
                                    errors.forEach(System.out::println);
                                    
                                    System.out.println("[ROLLBACK] Rolling back changes from recipe: " + recipe.name + " (Note: Please verify rollback manually for correctness)");
                                    // Rollback mechanism: Uses AST node tracking to restore original state should verify rollback completeness for complex transformations
                                    ctx.rollbackRecipe(recipe.name);
                                    ctx.markRolledBack(srcFile);
                                    ctx.recordRollbackError(srcFile, errors);
                                    
                                    // Backup rollback: restore entire file from original
                                    // Uncomment if granular recipe rollback proves insufficient
                                    
                                    // CompilationUnit originalCu = ctx.getOriginalFile(srcFile).orElse(null);
                                    // if (originalCu != null) {
                                    //     cu.replace(originalCu.clone());
                                    //     System.out.println("[ROLLBACK] Applied whole-file rollback for: " + recipe.name);
                                    // }
                                    
                                    // fileChanged remains true if other recipes modified the file
                                    Set<String> remainingRecipes = ctx.getRecipesForFile(srcFile);
                                    fileChanged = !remainingRecipes.isEmpty();
                                    
                                    // break;
                                    // TEST - Was causing early exit for recipes.
                                } else {
                                    ctx.registerRecipeForFile(srcFile, recipe.name);
                                    System.out.println("[VALIDATION] Recipe '" + recipe.name + "' passed validation using '" + recipe.rollbackOnError + "'");
                                }
                            } catch (IllegalArgumentException e) {
                                System.err.println("[VALIDATION] Unknown validator '" + recipe.rollbackOnError + "' in recipe '" + recipe.name + "'. Available validators: " + 
                                    String.join(", ", ValidationFactory.getAvailableValidators()));
                                // Continue without validation
                                ctx.registerRecipeForFile(srcFile, recipe.name);
                            }
                        } else {
                            ctx.registerRecipeForFile(srcFile, recipe.name);
                        }
                    }
                }

                if (fileChanged) {
                    List<String> finalParseIssues = performParseCheck(cu);
                    if (!finalParseIssues.isEmpty()) {
                        System.out.println("[FINAL-PARSE-CHECK] Issues detected in final transformed file: " + rel);
                        finalParseIssues.forEach(issue -> System.out.println("  [WARNING] " + issue));
                    } else {
                        System.out.println("[FINAL-PARSE-CHECK] [OK] Final transformed file passes parse validation: " + rel);
                    }
                }

                Path outFile = outputRoot.resolve(rel);
                Files.createDirectories(outFile.getParent());
                if (fileChanged) {
                    Files.writeString(outFile, cu.toString(), StandardCharsets.UTF_8);
                    System.out.println("[WRITE] Wrote transformed file: " + outFile);
                    ctx.markTransformed(srcFile);
                } else {
                    String originalContent = Files.readString(srcFile, StandardCharsets.UTF_8);
                    Files.writeString(outFile, originalContent, StandardCharsets.UTF_8);
                    System.out.println("[COPY] Wrote unmodified file: " + outFile);
                }
            }
        }
        System.out.println("\n=== Transformation Summary ===");

        System.out.println("\n[TRANSFORMED FILES] (" + ctx.getTransformedFiles().size() + ")");
        ctx.getTransformedFiles().forEach(f -> {
            Set<String> recipeLOG = ctx.getRecipesForFile(f);
            System.out.println("  ~ " + f + "  [Recipes: " + String.join(", ", recipeLOG) + "]");
        });

        System.out.println("\n[ROLLED BACK FILES] (" + ctx.getRolledBackFiles().size() + ")");
        ctx.getRolledBackFiles().forEach(f -> {
            System.out.println("  ~ " + f);
            List<ValidationError> errors = ctx.getRollbackErrors(f);
            for (ValidationError err : errors) {
                System.out.println("     =>> " + err);
            }
        });

        System.out.println("\n\n\n\n\n");

    }
}
