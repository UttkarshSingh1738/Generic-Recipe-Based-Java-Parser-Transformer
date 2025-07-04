package com.example.actions;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;
import gst.engine.actions.Action;

public class FactoryFieldInjectionAction implements Action {
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof FieldDeclaration fd)) return;
        // find the single VariableDeclarator
        var var = fd.getVariable(0);
        var init = var.getInitializer().orElse(null);
        if (!(init instanceof MethodCallExpr mc)) return;
        // second argument is the qualifier expression
        if (mc.getArguments().size() < 2) return;
        Expression qualifierExpr = mc.getArgument(1);

        ctx.saveOriginalNode(fd, fd.clone());

        fd.getModifiers().removeIf(m -> m.getKeyword().asString().equals("static") || m.getKeyword().asString().equals("final"));
        var.removeInitializer();

        fd.addAnnotation("Autowired");
        String qualText = qualifierExpr.toString();
            AnnotationExpr qualAnn = StaticJavaParser.parseAnnotation(
                "@Qualifier(" + qualText + ")"
            );
        fd.addAnnotation(qualAnn);

        System.out.println("[ACTION] factoryFieldInjection: qualifier=" + qualText);
    }
}
