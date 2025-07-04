package com.example.actions;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;
import gst.engine.actions.Action;

public class ComponentizeServiceImplAction implements Action {
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof ClassOrInterfaceDeclaration cd)) return;
        String cls = cd.getNameAsString();
        if (!cls.endsWith("SvcImpl")) return;
        if (cd.isAnnotationPresent("Component")) return;

        // infer beanName from interface (first implemented type)
        String beanName = cd.getImplementedTypes().stream()
            .map(t -> t.getNameAsString())
            .findFirst().orElseThrow();

        ctx.saveOriginalNode(cd, cd.clone());
        AnnotationExpr ann = StaticJavaParser.parseAnnotation(
            String.format("@Component(\"%s\")", beanName)
        );
        cd.addAnnotation(ann);
        System.out.println("[ACTION] componentizeService: added @Component(\"" + beanName + "\")");
    }
}