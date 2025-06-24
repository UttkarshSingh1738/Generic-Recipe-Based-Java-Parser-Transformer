package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class AddAnnotationAction implements Action {
    private final String name;
    public AddAnnotationAction(Map<String, String> params) {
        this.name = params.get("name");
    }
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (node instanceof NodeWithAnnotations<?> nwa) {
            ctx.saveOriginalNode(node, node.clone());
            nwa.addAnnotation(name);
            System.out.println("[ACTION] addAnnotation: " + name);
        }
    }
}