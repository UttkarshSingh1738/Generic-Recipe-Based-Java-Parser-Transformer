package gst.engine.actions;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class AddAnnotationToParentClassAction implements Action {

    private final String name;
    private final Map<String, String> attributes;

    @SuppressWarnings("unchecked")
    public AddAnnotationToParentClassAction(Map<String, Object> params) {
        this.name = (String) params.get("name");
        Object raw = params.get("attributes");
        this.attributes = raw == null
                ? null
                : ((Map<String, Object>) raw).entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().toString()
                ));
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (name == null || name.isBlank()) {
            return;
        }

        Optional<ClassOrInterfaceDeclaration> parentClassOpt = node.findAncestor(ClassOrInterfaceDeclaration.class);
        if (parentClassOpt.isEmpty()) {
            return;
        }

        ClassOrInterfaceDeclaration parentClass = parentClassOpt.get();
        if (parentClass.isAnnotationPresent(name)) {
            return; // Annotation already exists, do nothing.
        }

        ctx.saveOriginalNode(parentClass, parentClass.clone());

        AnnotationExpr newAnnotation;
        if (attributes == null || attributes.isEmpty()) {
            newAnnotation = StaticJavaParser.parseAnnotation("@" + name);
        } else {
            List<String> pairs = attributes.entrySet().stream()
                    .map(e -> {
                        String rawVal = e.getValue();
                        // if it doesn’t already look like a quoted literal, wrap in quotes:
                        if (!(rawVal.startsWith("\"") && rawVal.endsWith("\""))) {
                            rawVal = "\"" + rawVal
                                    .replace("\\", "\\\\")
                                    .replace("\"", "\\\"")
                                    + "\"";
                        }
                        return e.getKey() + " = " + rawVal;
                    }).collect(Collectors.toList());
            String inside = String.join(", ", pairs);
            newAnnotation = StaticJavaParser.parseAnnotation("@" + name + "(" + inside + ")");
        }

        parentClass.addAnnotation(newAnnotation);
        System.out.println("[ACTION] addAnnotationToParentClass: Added " + newAnnotation + " to class " + parentClass.getNameAsString());
    }
}
