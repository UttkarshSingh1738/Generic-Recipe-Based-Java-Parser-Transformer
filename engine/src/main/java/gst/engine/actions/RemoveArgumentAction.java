package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RemoveArgumentAction implements Action {
    private final int index;
    public RemoveArgumentAction(Map<String,String> params) {
        this.index = Integer.parseInt(params.get("index"));
    }
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof MethodCallExpr mc)) return;
        if (mc.getArguments().size() <= index) return;
        ctx.saveOriginalNode(mc, mc.clone());
        mc.getArgument(index).remove();
        System.out.println("[ACTION] removeArgument at index " + index);
    }
}