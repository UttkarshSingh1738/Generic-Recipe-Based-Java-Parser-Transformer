package com.yourorganization.maven_sample;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class GenericRecipeTransformer {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RecipeContainer {
        public List<Recipe> recipes;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Recipe {
        public String name;
        public String description;
        public ImportMods imports;
        public List<Step> steps;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ImportMods {
        public List<String> add;
        public List<String> remove;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Step {
        public Match match;
        // action: { actionName: { paramKey: paramVal, … } }
        public List<Map<String,Map<String,Object>>> actions;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Match {
        public String nodeType;
        public String fqn;
        public String type;
        public String methodName;
        public String fqnScope;
        public String annotation;
        public String matchExpr;
    }

    public static void main(String[] args) throws Exception {
        Path recipePath = Paths.get("src","main","resources","mappingsV2.json");
        Path inputRoot  = Paths.get("src","main","resources","input","guava","src");
        Path outputRoot = Paths.get("output","OutputV2Guava");

        ObjectMapper mapper = new ObjectMapper();
        RecipeContainer rc = mapper.readValue(recipePath.toFile(),
            new TypeReference<RecipeContainer>(){});
        List<Recipe> recipes = rc.recipes;

        Map<String,List<String>> recipeToFiles = new LinkedHashMap<>();
        for(Recipe r: recipes) recipeToFiles.put(r.name, new ArrayList<>());

        CombinedTypeSolver solver = new CombinedTypeSolver(
            new ReflectionTypeSolver(),
            new JavaParserTypeSolver(inputRoot.toFile())
        );
        JavaSymbolSolver sym = new JavaSymbolSolver(solver);
        ParserConfiguration cfg = new ParserConfiguration().setSymbolResolver(sym);
        StaticJavaParser.setConfiguration(cfg);

        // walking every .java in guava/src
        try(Stream<Path> stream = Files.walk(inputRoot)) {
            List<Path> javaFiles = stream.filter(p->p.toString().endsWith(".java")).collect(Collectors.toList());

            for(Path javaFile: javaFiles) {
                String rel = inputRoot.relativize(javaFile).toString();
                System.out.println("\n--- Processing: " + rel);

                // parse
                CompilationUnit cu;
                try {
                    cu = StaticJavaParser.parse(javaFile);
                } catch(IOException e) {
                    System.err.println("  parse error: " + e.getMessage());
                    continue;
                }

                boolean fileChanged = false;

                // apply each recipe in order
                for(Recipe recipe: recipes) {
                    System.out.println("  Recipe: " + recipe.name);

                    boolean matchedThisRecipe = false;

                    //  each step
                    for(Step step: recipe.steps) {
                        Match m = step.match;

                        if(m.matchExpr!=null) {
                            System.out.println("   matchExpr unsupported yet");
                            continue;
                        }

                        // gathering candidate nodes
                        List<? extends Node> candidates = switch(m.nodeType) {
                            case "ObjectCreationExpr"        -> cu.findAll(ObjectCreationExpr.class);
                            case "VariableDeclarationExpr"   -> cu.findAll(VariableDeclarationExpr.class);
                            case "MethodCallExpr"            -> cu.findAll(MethodCallExpr.class);
                            case "FieldAccessExpr"           -> cu.findAll(FieldAccessExpr.class);
                            case "AnnotationExpr"            -> cu.findAll(AnnotationExpr.class);
                            case "ImportDeclaration"         -> cu.findAll(ImportDeclaration.class);
                            case "NameExpr"                  -> cu.findAll(NameExpr.class);
                            case "Parameter"                 -> cu.findAll(Parameter.class);
                            case "ClassOrInterfaceType"      -> cu.findAll(ClassOrInterfaceType.class);
                            default -> {
                                System.out.println("  Unsupported nodeType: " + m.nodeType);
                                yield List.<Node>of();
                            }
                        };

                        for(Node n: candidates) {
                            if(matches(n, m, solver)) {
                                // apply actions
                                applyActions(n, step.actions, cu, solver);
                                // cu.getImports().removeIf(id ->
                                //     cu.findAll(NameExpr.class)
                                //     .stream()
                                //     .noneMatch(ne -> ne.toString().equals(id.getName().getIdentifier()))
                                // );
                                matchedThisRecipe = true;
                                fileChanged = true;
                            }
                        }
                    }

                    if(matchedThisRecipe) {
                        System.out.println("    matched & mutated");
                        recipeToFiles.get(recipe.name).add(rel);
                        
                        //  remove recipe‐level imports
                        if(recipe.imports!=null && recipe.imports.remove!=null) {
                            for(String imp: recipe.imports.remove) {
                                List<ImportDeclaration> toRm = cu.getImports().stream()
                                    .filter(id->id.getNameAsString().equals(imp))
                                    .collect(Collectors.toList());
                                for(ImportDeclaration id: toRm) {
                                    cu.getImports().remove(id);
                                    fileChanged = true;
                                }
                            }
                        }

                        //  add recipe‐level imports
                        if(recipe.imports!=null && recipe.imports.add!=null) {
                            for(String imp: recipe.imports.add) {
                                boolean already = cu.getImports().stream()
                                    .anyMatch(id->id.getNameAsString().equals(imp));
                                if(!already) {
                                    cu.addImport(imp);
                                    fileChanged = true;
                                }
                            }
                        }

                    } else {
                        System.out.println("    no matches");
                    }
                }
                
                // write out only if mutated
                if(fileChanged) {
                    Path outFile = outputRoot.resolve(rel);
                    Files.createDirectories(outFile.getParent());
                    Files.writeString(outFile, cu.toString());
                    System.out.println("  wrote: " + outFile);
                }
            }
        }

        System.out.println("\n=== Summary ===");
        for(Recipe r: recipes) {
            var lst = recipeToFiles.get(r.name);
            System.out.printf("Recipe '%s' matched %d files:%n", r.name, lst.size());
            for(String f: lst) System.out.println("  • " + f);
        }
        System.out.println("\nDone. Output in: " + outputRoot);
    }

    private static boolean matches(Node node, Match m, CombinedTypeSolver solver) {
        // sanity
        if (!node.getClass().getSimpleName().equals(m.nodeType)) {
            return false;
        }

        // Debug logging
        System.out.printf("[matches] %s (%s)  ➡  match.fqn=%s  match.type=%s%n",
            node, node.getClass().getSimpleName(),
            m.fqn, m.type);

        //fqn
        if (m.fqn != null) {
            String resolved = null;
            try {
                if (node instanceof ObjectCreationExpr oce) {
                    resolved = JavaParserFacade.get(solver)
                                .getType(oce)
                                .describe();
                } else if (node instanceof VariableDeclarationExpr vde) {
                    resolved = JavaParserFacade.get(solver)
                                .getType(vde.getElementType())
                                .describe();
                } else if (node instanceof Parameter p) {
                    resolved = JavaParserFacade.get(solver)
                                .getType(p.getType())
                                .describe();
                }
            } catch (Exception ignore) { }
            System.out.printf("    [fqn] resolved=%s%n", resolved);
            if (!m.fqn.equals(resolved)) {
                System.out.println("    [fqn] ✗ no match");
                return false;
            }
        }

        // `type`, match simple or resolved or ends-with
        if (m.type != null) {
            String simple   = null;
            String resolved = null;

            if (node instanceof VariableDeclarationExpr vde) {
                simple = vde.getElementType().asString();
                try {
                    resolved = vde.getElementType().resolve().asReferenceType().getQualifiedName();
                } catch (Exception ignored) { }
            }
            else if (node instanceof Parameter p) {
                simple = p.getType().asString();
                try {
                    resolved = p.getType().resolve().asReferenceType().getQualifiedName();
                } catch (Exception ignored) { }
            }

            System.out.printf("    [type] simple=%s   resolved=%s%n", simple, resolved);

            boolean typeMatches =
                // exact simple name
                (simple != null && simple.equals(m.type))
                // exact FQN
            || (resolved != null && resolved.equals(m.type))
                // FQN ends-with simple
            || (simple != null && m.type.endsWith("." + simple));

            if (!typeMatches) {
                System.out.println("    [type] ✗ no match");
                return false;
            }
        }

        // methodName
        if(m.methodName!=null) {
            if(!(node instanceof MethodCallExpr mc && mc.getNameAsString().equals(m.methodName)))
                return false;
        }
        // fqnScope
        if(m.fqnScope!=null) {
            if(!(node instanceof MethodCallExpr mc && mc.getScope().isPresent()))
                return false;
            try {
                String sf = JavaParserFacade.get(solver).getType(mc.getScope().get()).describe();
                if(!m.fqnScope.equals(sf)) return false;
            } catch(Exception ignore){ return false; }
        }
        // annotation
        if(m.annotation!=null) {
            if(node instanceof NodeWithAnnotations<?> nwa) {
                if(!nwa.isAnnotationPresent(m.annotation))
                    return false;
            } else return false;
        }
        // matchExpr stub
        if(m.matchExpr!=null) { return false; }
        
        return true;
    }

    @SuppressWarnings("unchecked")
    private static void applyActions(
            Node node,
            List<Map<String,Map<String,Object>>> actions,
            CompilationUnit cu,
            CombinedTypeSolver solver
    ) {
        for(var actionMap: actions) {
            for(var e: actionMap.entrySet()) {
                String name = e.getKey();
                Map<String,Object> p = e.getValue();

                switch(name) {
                    case "replaceWithMethodCall" -> {
                        String scope  = (String) p.get("scope");          // e.g. "LOGGER"
                        String method = (String) p.getOrDefault("method", "now");

                        if (node instanceof ObjectCreationExpr oce) {
                            oce.replace(new MethodCallExpr(new NameExpr(scope), method));
                        }
                        else if (node instanceof MethodCallExpr mc) {
                            // copy the original arguments
                            var newCall = new MethodCallExpr(new NameExpr(scope), method, mc.getArguments());
                            mc.replace(newCall);
                        }
                    }
                    case "changeType" -> {
                        String newType = (String)p.get("newType");
                        if(node instanceof VariableDeclarationExpr vde) {
                            vde.getVariables().forEach(v->v.setType(StaticJavaParser.parseType(newType)));
                        } else if(node instanceof ObjectCreationExpr oce2) {
                            oce2.setType(newType);
                        } else if (node instanceof Parameter prm) {
                            prm.setType(StaticJavaParser.parseType(newType));
                        }
                    }
                    case "renameMethod" -> {
                        if(node instanceof MethodCallExpr mc) {
                            String nm = (String)p.get("newName");
                            mc.setName(nm);
                        }
                    }
                    case "updateImport" -> {
                        var rem = (List<String>)p.get("remove");
                        if(rem!=null) for(String imp:rem)
                            cu.getImports().removeIf(id->id.getNameAsString().equals(imp));
                        var add = (List<String>)p.get("add");
                        if(add!=null) for(String imp:add)
                            cu.addImport(imp);
                    }
                    case "addAnnotation" -> {
                        if(node instanceof NodeWithAnnotations<?> nwa) {
                            nwa.addAnnotation((String)p.get("annotation"));
                        }
                    }
                    case "removeNode" -> {
                        if (node != null) node.remove();
                    }
                    case "customScript" -> {
                        // not implemented yet
                    }
                    default -> System.err.println("Unknown action: "+name);
                }
            }
        }
    }
}