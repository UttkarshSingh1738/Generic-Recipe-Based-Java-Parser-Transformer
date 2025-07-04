package com.example.actions;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;
import gst.engine.actions.Action;

public class FactoryLocalVarReplacementAction implements Action {
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof VariableDeclarationExpr vde)) return;
        var var = vde.getVariables().get(0);
        var init = var.getInitializer().orElse(null);
        if (!(init instanceof MethodCallExpr mc)) return;
        if (mc.getArguments().size() < 2) return;
        Expression qualifierExpr = mc.getArgument(1);

        ctx.saveOriginalNode(vde, vde.clone());

        var newCall = new MethodCallExpr(
            new NameExpr("SpringContext"),
            "getBean",
            NodeList.nodeList(qualifierExpr.clone())
        );
        var.setInitializer(newCall);

        System.out.println("[ACTION] factoryLocalVarReplacement: replaced init with SpringContext.getBean(" 
            + qualifierExpr + ")");
    }
}