package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class UpdateAnnotationAttributeAction implements Action {

    private final String annotation, attribute, newValue;

    public UpdateAnnotationAttributeAction(Map<String, String> p) {
        this.annotation = p.get("annotation");
        this.attribute = p.get("attribute");
        this.newValue = p.get("newValue");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver s) {
        if (node instanceof NodeWithAnnotations<?> nwa) {
            nwa.getAnnotationByName(annotation).ifPresent(ann -> {
                if (ann.isNormalAnnotationExpr()) {
                    ctx.saveOriginalNode(node, node.clone());
                    ann.asNormalAnnotationExpr().getPairs().stream()
                            .filter(pair -> pair.getNameAsString().equals(attribute))
                            .findFirst()
                            .ifPresent(pair -> pair.setValue(
                            StaticJavaParser.parseExpression("\"" + newValue + "\"")
                    ));
                    System.out.println("[ACTION] updateAnnotationAttribute: "
                            + annotation + "." + attribute + "=" + newValue);
                }
            });
        }
    }
}
