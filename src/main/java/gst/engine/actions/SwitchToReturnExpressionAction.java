package gst.engine.actions;

import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SwitchExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class SwitchToReturnExpressionAction implements Action {
    public SwitchToReturnExpressionAction(Map<String, String> params) {
        // no params needed
    }

    @Override
    public void apply(com.github.javaparser.ast.Node node,
                      CompilationUnit cu,
                      TxContext ctx,
                      JavaSymbolSolver solver) {
        if (!(node instanceof SwitchStmt sw)) return;
        ctx.saveOriginalNode(sw, sw.clone());

        // 1) Build the new SwitchExpr
        SwitchExpr sexpr = new SwitchExpr();
        sexpr.setSelector(sw.getSelector().clone());
        NodeList<SwitchEntry> newEntries = new NodeList<>();

        for (SwitchEntry oldEntry : sw.getEntries()) {
            // clone labels
            NodeList<Expression> labels = new NodeList<>(oldEntry.getLabels());

            // collect all statements *up to* the first break
            var body = oldEntry.getStatements().stream()
                .takeWhile(s -> !(s instanceof BreakStmt))
                .collect(Collectors.toList());

            // wrap multi-stmts in block, single-stmt inline
            SwitchEntry newEntry = new SwitchEntry();
            newEntry.setLabels(labels);
            if (body.size() == 1 && body.get(0) instanceof ReturnStmt) {
                // return X;  →  case → X
                var ret = (ReturnStmt) body.get(0);
                newEntry.setType(SwitchEntry.Type.EXPRESSION);
                newEntry.setStatements(new NodeList<>(new com.github.javaparser.ast.stmt.ExpressionStmt(ret.getExpression().orElseThrow())));
            } else {
                // arbitrary statements → { …; return …; }
                BlockStmt blk = new BlockStmt();
                blk.setStatements(new NodeList<>(body));
                newEntry.setType(SwitchEntry.Type.BLOCK);
                newEntry.setStatements(new NodeList<>(blk));
            }
            newEntries.add(newEntry);
        }

        sexpr.setEntries(newEntries);

        // 2) Replace the old switch *statement* with a *return* switch-expression
        ReturnStmt ret = new ReturnStmt(sexpr);
        sw.replace(ret);

        System.out.println("[ACTION] switch→return-expr: replaced SwitchStmt with return switch-expression");
    }
}
