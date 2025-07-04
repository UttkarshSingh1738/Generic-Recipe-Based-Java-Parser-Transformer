package com.example.actions;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;
import gst.engine.actions.Action;

public class AutowireQualifierFieldAction implements Action {

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof FieldDeclaration fd)) {
            return;
        }
        ctx.saveOriginalNode(fd, fd.clone());

        String qualifierExprString = null;
        if (fd.getVariables().size() == 1) {
            VariableDeclarator vd = fd.getVariables().get(0);
            if (vd.getInitializer().isPresent()) {
                Expression init = vd.getInitializer().get();
                if (init instanceof MethodCallExpr mce
                        && mce.getNameAsString().equals("getService")
                        && mce.getArguments().size() >= 2) {
                    qualifierExprString = mce.getArgument(1).toString();
                }
            }
        }

        fd.addMarkerAnnotation("Autowired");

        Expression valueExpr;
        if (qualifierExprString != null) {
            valueExpr = StaticJavaParser.parseExpression(qualifierExprString);
        } else {
            Type t = fd.getElementType();
            String typeName = t.isClassOrInterfaceType()
                    ? t.asClassOrInterfaceType().getNameAsString()
                    : t.toString();
            valueExpr = new StringLiteralExpr(typeName);
        }
        SingleMemberAnnotationExpr qualifier
                = new SingleMemberAnnotationExpr();
        qualifier.setName("Qualifier");
        qualifier.setMemberValue(valueExpr);

        fd.addAnnotation(qualifier);

        System.out.println("[ACTION] autowireQualifierField: @Qualifier("
                + (qualifierExprString != null
                        ? qualifierExprString
                        : "\"" + ((StringLiteralExpr) valueExpr).getValue() + "\"")
                + ")");
    }
}
