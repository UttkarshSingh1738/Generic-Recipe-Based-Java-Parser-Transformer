package com.example.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;
import gst.engine.actions.Action;

public class MyCustomAction implements Action {

    private final String foo;

    public MyCustomAction(Map<String, String> params) {
        this.foo = params.get("foo");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        ctx.saveOriginalNode(node, node.clone());
        node.setComment(new com.github.javaparser.ast.comments.LineComment(
                "[Inserted by MyCustomAction: foo=" + foo + "]"));
        System.out.println("[MyCustomAction] foo=" + foo + " on " + node.getClass().getSimpleName());
    }
}
