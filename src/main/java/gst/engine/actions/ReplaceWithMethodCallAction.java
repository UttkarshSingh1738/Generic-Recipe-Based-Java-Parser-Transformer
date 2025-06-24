package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class ReplaceWithMethodCallAction implements Action {
    private final String scope;
    private final String method;
    private final boolean includeScopeArg;
    private Boolean unwrapScope;

    public ReplaceWithMethodCallAction(Map<String, String> params) {
        this.scope = params.get("scope");
        this.method = params.getOrDefault("method", "now");
        this.includeScopeArg = Boolean.parseBoolean(params.getOrDefault("includeScopeArg", "false"));
        this.unwrapScope   = Boolean.valueOf(params.getOrDefault("unwrapScopeArg","false"));
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        ctx.saveOriginalNode(node, node.clone());

        if (node instanceof ObjectCreationExpr oce) {
            var replacement = new MethodCallExpr(new NameExpr(scope), method);
            oce.replace(replacement);
            System.out.println("[ACTION] Replaced 'new' expression with method call: " + scope + "." + method + "()");
        } else if (node instanceof MethodCallExpr mc) {
            NodeList<Expression> args = new NodeList<>();
            // Optionally include original scope as first argument
            if (unwrapScope && mc.getScope().filter(s -> s instanceof MethodCallExpr).isPresent()) {
                MethodCallExpr inner = (MethodCallExpr) mc.getScope().get();
                args.add(inner.getArgument(0).clone());
            } else if (includeScopeArg && mc.getScope().isPresent()) {
                args.add(mc.getScope().get().clone());
            }
            args.addAll(mc.getArguments());

            MethodCallExpr newCall = new MethodCallExpr(new NameExpr(scope), method, args);
            mc.replace(newCall);
            System.out.println("[ACTION] Replaced method call with: " + scope + "." + method + "(...)");
        }
    }

}
