package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class ReplaceWithScopeAction implements Action {
    public ReplaceWithScopeAction(Map<String, String> params) {
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof MethodCallExpr mc)) return;
        mc.getScope().ifPresent(scopeExpr -> {
            ctx.saveOriginalNode(mc, mc.clone());
            Expression repl = scopeExpr.clone();
            mc.replace(repl);
            System.out.println("[ACTION] Unwrapped method call, replaced with its scope: " + repl);
        });
    }
}
