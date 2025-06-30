package com.yourorganization.maven_sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MarkerAnnotationExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import com.github.javaparser.ast.visitor.Visitable;

public class Jdk8To17Transformer {
    public static void main(String[] args) throws IOException {
        // Load mapping from JSON
        ObjectMapper mapper = new ObjectMapper();
        InputStream input = Jdk8To17Transformer.class
                .getClassLoader()
                .getResourceAsStream("8-17-mapping.json");
        if (input == null) {
            throw new FileNotFoundException("8-17-mapping.json not found in resources!");
        }
        JsonNode root = mapper.readTree(input);

        // Parse annotations array into a structured map
        Map<String, List<String>> annotationMap = new HashMap<>();
        Map<String, String> annotationImportMap = new HashMap<>();
        for (JsonNode annNode : root.get("annotations")) {
            String oldAnn = annNode.get("oldAnnotation").asText();
            JsonNode newAnnNode = annNode.get("newAnnotation");
            List<String> replacements = new ArrayList<>();
            if (newAnnNode != null && !newAnnNode.isNull()) {
                if (newAnnNode.isArray()) {
                    newAnnNode.forEach(n -> replacements.add(n.asText()));
                } else {
                    replacements.add(newAnnNode.asText());
                }
            }
            annotationMap.put(oldAnn, replacements);

            JsonNode oldImportNode = annNode.get("oldImport");
            JsonNode newImportNode = annNode.get("newImport");
            if (oldImportNode != null && !oldImportNode.isNull()) {
                String oldImp = oldImportNode.asText();
                if (newImportNode == null || newImportNode.isNull()) {
                    annotationImportMap.put(oldImp, "");
                } else if (newImportNode.isArray()) {
                    List<String> imps = new ArrayList<>();
                    newImportNode.forEach(n -> imps.add(n.asText()));
                    annotationImportMap.put(oldImp, String.join(",", imps));
                } else {
                    annotationImportMap.put(oldImp, newImportNode.asText());
                }
            }
        }

        // 3. Parse imports mapping
        Map<String, String> importMap = new HashMap<>();
        root.get("imports").fields().forEachRemaining(e -> {
            importMap.put(e.getKey(), e.getValue().asText());
        });

        // 4. Parse static_class into maps
        Map<String, String> staticNameMap = new HashMap<>();
        Map<String, String> staticImportMap = new HashMap<>();
        for (JsonNode scNode : root.get("static_class")) {
            String oldClass = scNode.get("oldClassName").asText();
            String newClass = scNode.get("newClassName").asText();
            staticNameMap.put(oldClass, newClass);

            String oldImp = scNode.get("oldImport").asText();
            String newImp = scNode.get("newImport").asText();
            staticImportMap.put(oldImp, newImp);
        }

        // 5. Parse the Java file
        File inputFile = new File("src/main/resources/input/SampleController.java");
        JavaParser parser = new JavaParser();
        ParseResult<CompilationUnit> result = parser.parse(inputFile);
        if (!result.getResult().isPresent()) {
            throw new RuntimeException("Could not parse the source file.");
        }
        CompilationUnit cu = result.getResult().get();

        // 6. Apply transformations
        cu.accept(new UnifiedAnnotationTransformer(annotationMap, annotationImportMap), null);
        cu.accept(new ImportTransformer(importMap, annotationImportMap, staticImportMap), null);
        cu.accept(new StaticClassTransformer(staticNameMap), null);
        cu.accept(new ObjectAndMethodTransformer(), null);

        // 7. Write the modified code to an output file
        File outputFile = new File("output/TransformedSampleController.java");
        outputFile.getParentFile().mkdirs();
        try (java.io.FileWriter writer = new java.io.FileWriter(outputFile)) {
            writer.write(cu.toString());
        }
        System.out.println("Transformed file written to: " + outputFile.getPath());
    }

    /** AnnotationTransformer: handles annotation renames/removals. */
    static class UnifiedAnnotationTransformer extends ModifierVisitor<Void> {
        private final Map<String, List<String>> annotationMap;
        private final Map<String, String> annotationImportMap;
        
        UnifiedAnnotationTransformer(Map<String, List<String>> annotationMap,
                                     Map<String, String> annotationImportMap) {
            this.annotationMap = annotationMap;
            this.annotationImportMap = annotationImportMap;
            System.out.println("UnifiedAnnotationTransformer initialized with " +
                    annotationMap.size() + " annotations and " + annotationImportMap.size() + " imports.");
        }

        @Override
        public Visitable visit(MarkerAnnotationExpr n, Void arg) {
            String name = n.getNameAsString();
            if (annotationMap.containsKey(name)) {
                List<String> replacements = annotationMap.get(name);
                if (replacements.isEmpty()) {
                    n.remove();
                } else {
                    n.setName(replacements.get(0));
                    // Add any further replacements
                    if (replacements.size() > 1) {
                        Node parent = n.getParentNode().orElse(null);
                        if (parent instanceof NodeWithAnnotations) {
                            NodeWithAnnotations<?> nwa = (NodeWithAnnotations<?>) parent;
                            for (int i = 1; i < replacements.size(); i++) {
                                AnnotationExpr newAnn = new MarkerAnnotationExpr(replacements.get(i));
                                nwa.addAnnotation(newAnn);
                            }
                        }
                    }
                }
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(NormalAnnotationExpr n, Void arg) {
            String name = n.getNameAsString();
            if (annotationMap.containsKey(name)) {
                List<String> replacements = annotationMap.get(name);
                if (replacements.isEmpty()) {
                    n.remove();
                } else {
                    n.setName(replacements.get(0));
                    if (replacements.size() > 1) {
                        Node parent = n.getParentNode().orElse(null);
                        if (parent instanceof NodeWithAnnotations) {
                            NodeWithAnnotations<?> nwa = (NodeWithAnnotations<?>) parent;
                            for (int i = 1; i < replacements.size(); i++) {
                                AnnotationExpr newAnn = new MarkerAnnotationExpr(replacements.get(i));
                                nwa.addAnnotation(newAnn);
                            }
                        }
                    }
                }
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(SingleMemberAnnotationExpr n, Void arg) {
            String name = n.getNameAsString();
            if (annotationMap.containsKey(name)) {
                List<String> replacements = annotationMap.get(name);
                if (replacements.isEmpty()) {
                    n.remove();
                } else {
                    n.setName(replacements.get(0));
                    if (replacements.size() > 1) {
                        Node parent = n.getParentNode().orElse(null);
                        if (parent instanceof NodeWithAnnotations) {
                            NodeWithAnnotations<?> nwa = (NodeWithAnnotations<?>) parent;
                            for (int i = 1; i < replacements.size(); i++) {
                                AnnotationExpr newAnn = new MarkerAnnotationExpr(replacements.get(i));
                                nwa.addAnnotation(newAnn);
                            }
                        }
                    }
                }
            }
            return super.visit(n, arg);
        }
    }

    /** ImportTransformer: handles import updates/removals. */
    static class ImportTransformer extends ModifierVisitor<Void> {
        private final Map<String, String> importMap;
        private final Map<String, String> annotationImportMap;
        private final Map<String, String> staticImportMap;

        ImportTransformer(Map<String, String> importMap,
                          Map<String, String> annotationImportMap,
                          Map<String, String> staticImportMap) {
            this.importMap = importMap;
            this.annotationImportMap = annotationImportMap;
            this.staticImportMap = staticImportMap;
        }

        @Override
        public ImportDeclaration visit(ImportDeclaration impDecl, Void arg) {
            String fullName = impDecl.getNameAsString();

            // 1) Check general imports
            if (importMap.containsKey(fullName)) {
                String replacement = importMap.get(fullName);
                if (replacement.isEmpty()) {
                    return null; // remove import
                }
                impDecl.setName(replacement);
                return impDecl;
            }

            // 2) Check annotation‐related imports
            if (annotationImportMap.containsKey(fullName)) {
                String replacement = annotationImportMap.get(fullName);
                if (replacement.isEmpty()) {
                    return null;
                }
                if (replacement.contains(",")) {
                    String[] parts = replacement.split(",");
                    impDecl.setName(parts[0]);
                    for (int i = 1; i < parts.length; i++) {
                        ImportDeclaration extra = new ImportDeclaration(parts[i].trim(), false, false);
                        impDecl.getParentNode().ifPresent(parent -> {
                            if (parent instanceof CompilationUnit) {
                                ((CompilationUnit) parent).addImport(extra);
                            }
                        });
                    }
                } else {
                    impDecl.setName(replacement);
                }
                return impDecl;
            }

            // 3) Check static‐class imports
            if (staticImportMap.containsKey(fullName)) {
                String replacement = staticImportMap.get(fullName);
                if (replacement.isEmpty()) {
                    return null;
                }
                impDecl.setName(replacement);
                return impDecl;
            }

            return impDecl;
        }
    }

    /** StaticClassTransformer: renames class/type usages. */
    static class StaticClassTransformer extends ModifierVisitor<Void> {
        private final Map<String, String> staticNameMap;

        StaticClassTransformer(Map<String, String> staticNameMap) {
            this.staticNameMap = staticNameMap;
        }

        @Override
        public Visitable visit(NameExpr n, Void arg) {
            String name = n.getNameAsString();
            if (staticNameMap.containsKey(name)) {
                n.setName(staticNameMap.get(name));
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(ClassOrInterfaceType cit, Void arg) {
            String name = cit.getNameAsString();
            if (staticNameMap.containsKey(name)) {
                cit.setName(staticNameMap.get(name));
            }
            return super.visit(cit, arg);
        }
    }

    /** ObjectAndMethodTransformer: rewrites object creation/method calls. */
    static class ObjectAndMethodTransformer extends ModifierVisitor<Void> {
        @Override
        public Visitable visit(ObjectCreationExpr n, Void arg) {
            String type = n.getTypeAsString();
            if (type.equals("DefaultHttpClient")) {
                n.setType("HttpClient");
                return n;
            }
            if (type.equals("Date")) {
                return new MethodCallExpr("LocalDate.now");
            }
            if (type.equals("SimpleDateFormat")) {
                if (!n.getArguments().isEmpty()) {
                    Expression patternArg = n.getArguments().get(0);
                    return new MethodCallExpr(
                            new NameExpr("DateTimeFormatter"),
                            "ofPattern",
                            NodeList.nodeList(patternArg)
                    );
                }
            }
            return super.visit(n, arg);
        }

        @Override
        public Visitable visit(MethodCallExpr n, Void arg) {
            if (n.getNameAsString().equals("getInstance")
                    && n.getScope().isPresent()
                    && n.getScope().get().toString().equals("Calendar")) {
                return new MethodCallExpr("ZonedDateTime.now");
            }
            return super.visit(n, arg);
        }
    }
}
