package gst.engine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

import gst.api.ImportMods;
import gst.api.MappingLoader;
import gst.api.Match;
import gst.api.Recipe;
import gst.api.Step;
import gst.engine.actions.Action;
import gst.engine.actions.ActionFactory;
import gst.engine.matcher.NodeMatcher;
import gst.engine.validator.ValidationError;
import gst.engine.validator.Validator;

public class Pipeline {

    public static void run(Path mappingFile, Path inputRoot, Path outputRoot) throws IOException {
        List<Recipe> recipes = MappingLoader.load(mappingFile);

        CombinedTypeSolver typeSolver = new CombinedTypeSolver(
                new ReflectionTypeSolver(),
                new JavaParserTypeSolver(inputRoot.toFile())
        );
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration cfg = new ParserConfiguration().setSymbolResolver(symbolSolver);
        StaticJavaParser.setConfiguration(cfg);

        TxContext ctx = new TxContext();

        try (Stream<Path> files = Files.walk(inputRoot)) {
            List<Path> javaFiles = files
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());

            for (Path srcFile : javaFiles) {
                String rel = inputRoot.relativize(srcFile).toString();
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
                            if (NodeMatcher.matches(node, m, typeSolver)) {
                                System.out.println("[MATCH] " + m.nodeType + " at " + node.getRange().orElse(null));
                                for (var actionMap : step.actions) {
                                    actionMap.forEach((actionName, params) -> {
                                        ctx.saveOriginalNode(node, node.clone());
                                        Action act = ActionFactory.create(actionName, params);
                                        System.out.println("[ACTION] " + actionName + " on node at " + node.getRange().orElse(null));
                                        act.apply(node, cu, ctx, symbolSolver);
                                        ctx.registerRecipeChange(recipe.name, node);
                                    });
                                }
                                matchedRecipe = true;
                                fileChanged = true;
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
                        ctx.markFileChanged(srcFile);
                        ctx.registerRecipeForFile(srcFile, recipe.name);
                    }
                }

                if (ctx.isFileChanged(srcFile)) {
                    List<ValidationError> errors = Validator.run(List.of(cu), ctx, symbolSolver);

                    if (!errors.isEmpty()) {
                        System.out.println("[VALIDATION] Errors found in file: " + rel);
                        errors.forEach(System.out::println);

                        // 1) Which recipes actually ran on this file?
                        Set<String> applied = ctx.getRecipesForFile(srcFile);

                        // 2) Of those, which want rollback?
                        Optional<Recipe> toRollback = recipes.stream()
                            .filter(r -> r.rollbackOnError)
                            .filter(r -> applied.contains(r.name))
                            .findFirst();

                        if (toRollback.isPresent()) {
                        Recipe bad = toRollback.get();
                        System.out.println("[ROLLBACK] Rolling back changes due to validation failure in recipe: "
                                            + bad.name);
                        ctx.getOriginalFile(srcFile).ifPresent(original -> {
                            cu.setPackageDeclaration(original.getPackageDeclaration().orElse(null));
                            cu.setImports(original.getImports());
                            cu.setTypes(original.getTypes());
                        });
                        ctx.markRolledBack(srcFile);
                        ctx.recordRollbackError(srcFile, errors);
                        fileChanged = false;
                        } else {
                        // No matching recipe wanted rollback — proceed with write
                        System.out.println("[WARNING] Validation errors found, but no applied recipe had rollbackOnError=true; keeping changes.");
                        }
                    }
                    }

                if (fileChanged) {
                    Path outFile = outputRoot.resolve(rel);
                    Files.createDirectories(outFile.getParent());
                    Files.writeString(outFile, cu.toString(), StandardCharsets.UTF_8);
                    System.out.println("[WRITE] Wrote transformed file: " + outFile);
                    ctx.markTransformed(srcFile);

                } else {
                    System.out.println("[SKIP] No changes written for: " + rel);
                }
            }
        }
        System.out.println("\n=== Transformation Summary ===");

        System.out.println("\n[TRANSFORMED FILES]");
        ctx.getTransformedFiles().forEach(f -> {
            Set<String> recipeLOG = ctx.getRecipesForFile(f);
            System.out.println("  ~ " + f + "  [Recipes: " + String.join(", ", recipeLOG) + "]");
        });

        System.out.println("\n[ROLLED BACK FILES]");
        ctx.getRolledBackFiles().forEach(f -> {
            System.out.println("  ~ " + f);
            List<ValidationError> errors = ctx.getRollbackErrors(f);
            for (ValidationError err : errors) {
                System.out.println("     =>> " + err);
            }
        });

    }

    public static void main(String[] args) throws Exception {
        Path mappingFile = Paths.get("src", "main", "resources", "mappingsV3.json");
        Path inputRoot = Paths.get("src", "main", "resources", "input", "guava", "src");
        Path outputRoot = Paths.get("output", "OutputGST");

        run(mappingFile, inputRoot, outputRoot);
    }
}
