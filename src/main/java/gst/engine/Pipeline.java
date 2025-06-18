package gst.engine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
        // 1) Load recipes
        List<Recipe> recipes = MappingLoader.load(mappingFile);

        // 2) Setup JavaParser + SymbolSolver
        CombinedTypeSolver typeSolver = new CombinedTypeSolver(
            new ReflectionTypeSolver(),
            new JavaParserTypeSolver(inputRoot.toFile())
        );
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(typeSolver);
        ParserConfiguration cfg = new ParserConfiguration().setSymbolResolver(symbolSolver);
        StaticJavaParser.setConfiguration(cfg);

        // 3) Prepare context
        TxContext ctx = new TxContext();

        // 4) Walk all .java files
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
                    System.err.println("Parse error: " + ex.getMessage());
                    continue;
                }

                boolean fileChanged = false;

                // 5) For each recipe
                for (Recipe recipe : recipes) {
                    boolean matchedRecipe = false;

                    // 5a) Apply each step
                    for (Step step : recipe.steps) {
                        Match m = step.match;
                        List<Node> candidates = NodeMatcher.findCandidates(cu, m.nodeType);

                        for (Node node : candidates) {
                            if (NodeMatcher.matches(node, m, typeSolver)) {
                                // apply each action
                                for (var actionMap : step.actions) {
                                    actionMap.forEach((actionName, params) -> {
                                        Action act = ActionFactory.create(actionName, params);
                                        act.apply(node, cu, ctx, symbolSolver);
                                    });
                                }
                                matchedRecipe = true;
                                fileChanged = true;
                            }
                        }
                    }

                    // 5b) If any step matched, handle imports
                    if (matchedRecipe && recipe.imports != null) {
                        ImportMods im = recipe.imports;
                        if (im.remove != null) {
                            cu.getImports().removeIf(id -> im.remove.contains(id.getNameAsString()));
                            fileChanged = true;
                        }
                        if (im.add != null) {
                            for (String imp : im.add) {
                                boolean present = cu.getImports()
                                    .stream()
                                    .anyMatch(i -> i.getNameAsString().equals(imp));
                                if (!present) {
                                    cu.addImport(imp);
                                    fileChanged = true;
                                }
                            }
                        }
                    }
                }

                List<CompilationUnit> unitsList = List.of(cu);
                List<ValidationError> errors = Validator.run(unitsList, ctx, symbolSolver);
                if (!errors.isEmpty()) {
                    System.out.println("\nValidation errors:");
                    errors.forEach(System.out::println);
                }

                // 6) Write out mutated file
                if (fileChanged) {
                    Path outFile = outputRoot.resolve(rel);
                    Files.createDirectories(outFile.getParent());
                    Files.writeString(outFile, cu.toString(), StandardCharsets.UTF_8);
                    System.out.println("  Wrote: " + outFile);
                }
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Path mappingFile = Paths.get("src","main","resources","mappingsV3.json");
        Path inputRoot   = Paths.get("src","main","resources","input","guava","src");
        Path outputRoot  = Paths.get("output","OutputGST");

        run(mappingFile, inputRoot, outputRoot);
    }
}
