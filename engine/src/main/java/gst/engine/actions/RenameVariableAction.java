package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RenameVariableAction implements Action {
    private final String oldName;
    private final String newName;

    public RenameVariableAction(Map<String, String> params) {
        this.oldName = params.get("oldName");
        this.newName = params.get("newName");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (node instanceof FieldDeclaration fd) {
            for (VariableDeclarator vd : fd.getVariables()) {
                if (vd.getNameAsString().equals(oldName)) {
                    ctx.saveOriginalNode(vd, vd.clone());
                    vd.setName(newName);
                    System.out.println("[ACTION] Renamed field var: " + oldName + " → " + newName);
                }
            }
        }
        else if (node instanceof VariableDeclarator vd && vd.getNameAsString().equals(oldName)) {
            ctx.saveOriginalNode(vd, vd.clone());
            vd.setName(newName);
            System.out.println("[ACTION] Renamed local var: " + oldName + " → " + newName);
        }
        else if (node instanceof Parameter p && p.getNameAsString().equals(oldName)) {
            ctx.saveOriginalNode(p, p.clone());
            p.setName(newName);
            System.out.println("[ACTION] Renamed parameter: " + oldName + " → " + newName);
        }

        cu.findAll(NameExpr.class, ne -> ne.getNameAsString().equals(oldName))
          .forEach(ne -> {
              ctx.saveOriginalNode(ne, ne.clone());
              ne.setName(newName);
              System.out.println("[ACTION] Renamed reference: " + oldName + " → " + newName);
          });
    }
}
