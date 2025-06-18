package com.yourorganization.maven_sample;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

/**
 * ──────────────────────────────────────────────────────────────────────────────
 * MATCH PREDICATES (in "match" section)
 * ──────────────────────────────────────────────────────────────────────────────
 * importIs               – exact import declaration to match (ImportDeclaration)
 * nameIs                 – exact name (AnnotationExpr or NameExpr)
 * typeIs                 – exact type name in an ObjectCreationExpr
 * scopeIs                – exact textual scope in a MethodCallExpr
 * resolveFqnIs           – fully‐qualified name (FQN) of a ClassOrInterfaceType,
 *                          NameExpr, or ObjectCreationExpr target
 * resolveScopeFqnIs      – FQN of the scope expression in a MethodCallExpr
 *
 * ──────────────────────────────────────────────────────────────────────────────
 * ACTION KEYS (in "action" section)
 * ──────────────────────────────────────────────────────────────────────────────
 * replaceImport          – rename an import declaration
 * removeImport           – delete an import declaration node
 * addImport              – enqueue one or more imports to be added at the top
 *
 * replaceName            – rename an annotation (AnnotationExpr)
 * removeAnnotation       – delete an annotation node
 * addAnnotations         – add one or more annotations to the parent node
 *
 * changeType             – rename a type (ClassOrInterfaceType or ObjectCreationExpr)
 *
 * replaceWithMethodCall  – replace the entire node with a MethodCallExpr,
 *                          e.g. "LocalDate.now"
 *
 * replaceWithMethodCallTemplate
 *                        – templated replacement for ObjectCreationExpr, with:
 *                          • scope         (e.g. "DateTimeFormatter")
 *                          • method        (e.g. "ofPattern")
 *                          • useOriginalArgs (true/false)
 *
 * ──────────────────────────────────────────────────────────────────────────────
 */

public class GenericASTTransformer {

    public static class Rule {
        public String nodeType;
        public Map<String,Object> match;
        public Map<String,Object> action;
    }

    private final List<Rule> rules;
    private final CombinedTypeSolver typeSolver;

    public GenericASTTransformer(List<Rule> rules, CombinedTypeSolver solver) {
        this.rules = rules;
        this.typeSolver = solver;
    }

    public void transform(CompilationUnit cu) {
        Set<String> toImport = new HashSet<>();

        cu.walk(node -> {
            String type = node.getClass().getSimpleName();
            for (Rule r : rules) {
                if (r.nodeType.equals(type) && matches(r.match, node)) {
                    apply(r.action, node, toImport);
                }
            }
        });

        toImport.forEach(cu::addImport);
    }

    private boolean matches(Map<String,Object> m, Node n) {
        for (var e : m.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();

            switch (key) {
                case "importIs":
                    if (!(n instanceof ImportDeclaration)) return false;
                    if (!((ImportDeclaration)n).getNameAsString().equals(val)) return false;
                    break;

                case "nameIs":
                switch (n) {
                    case AnnotationExpr annotationExpr -> {
                        if (!annotationExpr.getNameAsString().equals(val)) return false;
                    }
                    case NameExpr nameExpr -> {
                        if (!nameExpr.getNameAsString().equals(val)) return false;
                    }
                    default -> {
                        return false;
                    }
                }
                    break;



                case "typeIs":
                    if (!(n instanceof ObjectCreationExpr)) return false;
                    if (!((ObjectCreationExpr)n).getTypeAsString().equals(val)) return false;
                    break;

                case "scopeIs":
                    if (!(n instanceof MethodCallExpr)) return false;
                    var scope = ((MethodCallExpr)n).getScope();
                    if (scope.isEmpty() || !scope.get().toString().equals(val)) return false;
                    break;

                case "resolveFqnIs":
                    String fq = null;
                    try {
                        if (n instanceof ClassOrInterfaceType cit) {
                            var rr = JavaParserFacade.get(typeSolver).getType(cit);
                            fq = rr.describe();
                        } else if (n instanceof NameExpr ne) {
                            ResolvedReferenceTypeDeclaration decl = JavaParserFacade.get(typeSolver).getTypeDeclaration(ne);
                            fq = decl.getQualifiedName();
                        } else if (n instanceof ObjectCreationExpr oce) {
                            fq = JavaParserFacade.get(typeSolver).getType(oce.getType()).describe();
                        }
                    } catch (Exception x) {
                        return false;
                    }
                    if (!val.equals(fq)) return false;
                    break;

                case "resolveScopeFqnIs":
                    if (!(n instanceof MethodCallExpr mce) || mce.getScope().isEmpty()) return false;
                    Expression sc = mce.getScope().get();
                    String sfq;
                    try {
                        sfq = JavaParserFacade.get(typeSolver).getType(sc).describe();
                    } catch (Exception x) {
                        return false;
                    }
                    if (!val.equals(sfq)) return false;
                    break;

                default:
                    return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    private void apply(Map<String,Object> a, Node n, Set<String> toImport) {
        for (var e : a.entrySet()) {
            String key = e.getKey();
            Object val = e.getValue();

            switch (key) {
                case "replaceImport":
                    ((ImportDeclaration)n).setName((String)val);
                    break;

                case "removeImport":
                    n.remove();
                    break;

                case "addImport":
                    if (val instanceof List<?> lst) {
                        lst.forEach(i -> toImport.add((String)i));
                    } else {
                        toImport.add((String)val);
                    }
                    break;

                case "replaceName":
                    ((AnnotationExpr)n).setName((String)val);
                    break;

                case "removeAnnotation":
                    n.remove();
                    break;

                case "addAnnotations":
                    var parent = n.getParentNode().orElseThrow();
                    if (parent instanceof NodeWithAnnotations<?> nwa) {
                        for (String ann : (List<String>)val) {
                            nwa.addAnnotation(ann);
                        }
                    }
                    break;

                case "changeType":
                    if (n instanceof ClassOrInterfaceType cit) {
                        cit.setName((String)val);
                    } else if (n instanceof ObjectCreationExpr oce) {
                        oce.setType((String)val);
                    }
                    break;

                case "replaceWithMethodCall":
                    String[] parts = ((String)val).split("\\.");
                    MethodCallExpr call = parts.length == 2
                        ? new MethodCallExpr(new NameExpr(parts[0]), parts[1])
                        : new MethodCallExpr(parts[0]);
                    n.replace(call);
                    break;

                case "replaceWithMethodCallTemplate":
                    var map = (Map<String,Object>)val;
                    String scope  = (String) map.get("scope");
                    String method = (String) map.get("method");
                    boolean keepArgs = Boolean.TRUE.equals(map.get("useOriginalArgs"));

                    if (n instanceof ObjectCreationExpr oce2) {
                        NodeList<Expression> args = keepArgs ? oce2.getArguments() : new NodeList<>();

                        MethodCallExpr tplCall = new MethodCallExpr(
                            new NameExpr(scope),
                            new SimpleName(method),
                            args
                        );
                        n.replace(tplCall);
                    }
                    break;           

                }
        }
    }

    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        InputStream in = GenericASTTransformer.class
            .getClassLoader().getResourceAsStream("mappings.json");
        TypeReference<Map<String,List<Rule>>> mapType = new TypeReference<>() {};
        Map<String,List<Rule>> byType = mapper.readValue(in, mapType);

        List<Rule> rules = new ArrayList<>();
        byType.forEach((nodeType, list) ->
            list.forEach(r -> { r.nodeType = nodeType; rules.add(r); })
        );

        CombinedTypeSolver solver = new CombinedTypeSolver(new ReflectionTypeSolver());
        JavaSymbolSolver sym = new JavaSymbolSolver(solver);
        ParserConfiguration config = new ParserConfiguration().setSymbolResolver(sym);
        JavaParser parser = new JavaParser(config);

        Path inputDir  = Paths.get("src/main/resources/input/SampleController.java");
        Path outputDir = Paths.get("output");
        Files.createDirectories(outputDir);

        try (var stream = Files.walk(inputDir)) {
            stream.filter(p -> p.toString().endsWith(".java"))
                .forEach(srcPath -> {
                try {
                    ParseResult<CompilationUnit> res = parser.parse(srcPath);
                    CompilationUnit cu = res.getResult().orElseThrow();

                    new GenericASTTransformer(rules, solver).transform(cu);

                    String originalName = srcPath.getFileName().toString();
                    String transformedName = "Transformed" + originalName;
                    Path outPath = outputDir.resolve(transformedName);

                    Files.writeString(outPath, cu.toString());
                    System.out.println("Wrote: " + outPath);
                } catch (IOException e) {
                    System.err.println("FAILED on " + srcPath + ": " + e.getMessage());
                }
            });
        }
    }
}
