package com.example.actions;

import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;
import gst.engine.actions.Action;

public class ChangeMethodReturnTypeAction implements Action {
    private final String newType;

    public ChangeMethodReturnTypeAction(Map<String,String> params) {
        this.newType = params.get("newType");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof MethodDeclaration md)) return;
        ctx.saveOriginalNode(md, md.clone());

        md.setType(StaticJavaParser.parseType(newType));
        System.out.println("[ACTION] changeMethodReturnType to '" + newType + "'");
    }
}
