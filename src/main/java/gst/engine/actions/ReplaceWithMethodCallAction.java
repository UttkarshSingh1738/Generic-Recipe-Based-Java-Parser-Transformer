package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class ReplaceWithMethodCallAction implements Action {
    private final String scope;
    private final String method;

    public ReplaceWithMethodCallAction(Map<String, String> params) {
        this.scope = params.get("scope");
        this.method = params.getOrDefault("method", "now");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        ctx.saveOriginalNode(node, node.clone());

        if (node instanceof ObjectCreationExpr oce) {
            var replacement = new MethodCallExpr(new NameExpr(scope), method);
            oce.replace(replacement);
            System.out.println("[ACTION] Replaced 'new' expression with method call: " + scope + "." + method + "()");
        } else if (node instanceof MethodCallExpr mc) {
            var newCall = new MethodCallExpr(new NameExpr(scope), method, mc.getArguments());
            mc.replace(newCall);
            System.out.println("[ACTION] Replaced method call with: " + scope + "." + method + "(...)");
        }
    }

}
