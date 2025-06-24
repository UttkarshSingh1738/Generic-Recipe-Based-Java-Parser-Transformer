package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RemoveNodeAction implements Action {
    public RemoveNodeAction(Map<String, String> params) {}
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        ctx.saveOriginalNode(node, node.clone());
        node.remove();
        System.out.println("[ACTION] removeNode: " + node.getClass().getSimpleName());
    }
}