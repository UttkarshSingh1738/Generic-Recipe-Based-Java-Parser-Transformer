package gst.engine.actions;

import java.util.Map;
import java.util.regex.Pattern;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RemoveStatementsAction implements Action {
    private final String matchExpr;
    public RemoveStatementsAction(Map<String,String> params) {
        this.matchExpr = params.get("matchExpr");
    }
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof BlockStmt block)) return;
        ctx.saveOriginalNode(block, block.clone());
        block.getStatements().removeIf(stmt ->
            Pattern.compile(matchExpr).matcher(stmt.toString()).find()
        );
        System.out.println("[ACTION] removeStatements matching `" + matchExpr + "`");
    }
}
