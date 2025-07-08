package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RemoveAnnotationAction implements Action {

    private final String name;

    public RemoveAnnotationAction(Map<String, String> params) {
        this.name = params.get("name");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof NodeWithAnnotations<?> nwa)) {
            return;
        }
        ctx.saveOriginalNode(node, node.clone());
        nwa.getAnnotations().stream()
                .filter(a -> a.getNameAsString().equals(name))
                .findFirst()
                .ifPresent(AnnotationExpr::remove);
        System.out.println("[ACTION] removeAnnotation: @" + name);
    }
}
