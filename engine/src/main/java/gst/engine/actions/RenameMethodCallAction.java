package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RenameMethodCallAction implements Action {
    private final String oldName, newName;
    public RenameMethodCallAction(Map<String,String> params) {
        this.oldName = params.get("oldName");
        this.newName = params.get("newName");
    }
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof MethodCallExpr mc)) return;
        if (!mc.getNameAsString().equals(oldName)) return;
        ctx.saveOriginalNode(mc, mc.clone());
        mc.setName(newName);
        System.out.println("[ACTION] renameMethodCall: " + oldName + " → " + newName);
    }
}
