package com.example.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;
import gst.engine.actions.Action;

public class RenameMethodAction implements Action {
    private final String newName;
    public RenameMethodAction(Map<String,String> params) {
        this.newName = params.get("newName");
    }
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof MethodDeclaration md)) return;
        ctx.saveOriginalNode(md, md.clone());
        md.setName(newName);
        System.out.println("[ACTION] renameMethod to '" + newName + "'");
    }
}