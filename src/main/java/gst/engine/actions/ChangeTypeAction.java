package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class ChangeTypeAction implements Action {
    private final String newType;

    public ChangeTypeAction(Map<String, String> params) {
        this.newType = params.get("newType");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (node instanceof VariableDeclarationExpr vde) {
            vde.getVariables().forEach(v -> {
                v.setType(StaticJavaParser.parseType(newType));
                ctx.registerVarType(v.getNameAsString(), newType);
            });
        }
        else if (node instanceof Parameter prm) {
            prm.setType(StaticJavaParser.parseType(newType));
            ctx.registerVarType(prm.getNameAsString(), newType);
        }
        else if (node instanceof ObjectCreationExpr oce) {
            // e.g. changing "new Date()" → "new Foo()"
            Type t = StaticJavaParser.parseType(newType);
            if (t.isClassOrInterfaceType()) {
                oce.setType(t.asClassOrInterfaceType());
            }
        }
    }
}
