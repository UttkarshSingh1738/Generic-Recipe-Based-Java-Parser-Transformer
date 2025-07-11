package gst.engine.actions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class AddAnnotationAction implements Action {

    private final String name;
    private final Map<String, String> attributes;  // may be null

    @SuppressWarnings("unchecked")
    public AddAnnotationAction(Map<String, Object> params) {
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
        if (!(node instanceof NodeWithAnnotations<?> nwa)) {
            return;
        }
        ctx.saveOriginalNode(node, node.clone());

        AnnotationExpr ann;
        if (attributes == null || attributes.isEmpty()) {
            ann = StaticJavaParser.parseAnnotation("@" + name);
        } else {
            List<String> pairs = attributes.entrySet().stream()
                    .map(e -> e.getKey() + " = " + e.getValue())
                    .collect(Collectors.toList());
            String inside = String.join(", ", pairs);
            ann = StaticJavaParser.parseAnnotation("@" + name + "(" + inside + ")");
        }

        nwa.addAnnotation(ann);
        System.out.println("[ACTION] addAnnotation: " + ann);
    }
}
