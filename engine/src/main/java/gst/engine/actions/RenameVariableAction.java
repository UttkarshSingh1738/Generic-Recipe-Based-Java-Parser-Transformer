package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RenameVariableAction implements Action {
    private final String newName;

    public RenameVariableAction(Map<String, String> params) {
        this.newName = params.get("newName");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof VariableDeclarator vd)) return;
        String oldName = vd.getNameAsString();

        ctx.saveOriginalNode(vd, vd.clone());

        vd.setName(newName);
        System.out.println("[ACTION] renameVariable: " + oldName + " → " + newName);

        cu.accept(new VoidVisitorAdapter<Void>() {
            @Override
            public void visit(NameExpr ne, Void arg) {
                super.visit(ne, arg);
                if (ne.getNameAsString().equals(oldName)) {
                    ne.setName(newName);
                }
            }
        }, null);
    }
}
