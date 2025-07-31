package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RemoveParentNodeAction implements Action {
    public RemoveParentNodeAction(Map<String, String> params) {}
    
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        Node parent = node.getParentNode().orElse(null);
        if (parent != null) {
            ctx.saveOriginalNode(parent, parent.clone());
            parent.remove();
            System.out.println("[ACTION] removeParentNode: " + parent.getClass().getSimpleName());
        }
    }
}
