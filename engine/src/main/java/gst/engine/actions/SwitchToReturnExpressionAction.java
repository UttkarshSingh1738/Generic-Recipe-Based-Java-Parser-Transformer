package gst.engine.actions;

import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.SwitchExpr;
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

        if (!canConvertToSwitchExpression(sw)) {
            System.out.println("[SKIP] Switch statement cannot be safely converted to switch expression");
            return;
        }

        SwitchExpr sexpr = new SwitchExpr();
        sexpr.setSelector(sw.getSelector().clone());
        NodeList<SwitchEntry> newEntries = new NodeList<>();

        for (SwitchEntry oldEntry : sw.getEntries()) {
            NodeList<Expression> labels = new NodeList<>(oldEntry.getLabels());

            var body = oldEntry.getStatements().stream()
                .takeWhile(s -> !(s instanceof BreakStmt))
                .collect(Collectors.toList());

            if (body.isEmpty()) {
                System.out.println("[SKIP] Empty switch case cannot be converted to switch expression");
                return;
            }

            SwitchEntry newEntry = new SwitchEntry();
            newEntry.setLabels(labels);
            
            if (body.size() == 1 && body.get(0) instanceof ReturnStmt) {
                var ret = (ReturnStmt) body.get(0);
                if (ret.getExpression().isPresent()) {
                    newEntry.setType(SwitchEntry.Type.EXPRESSION);
                    newEntry.setStatements(new NodeList<>(new com.github.javaparser.ast.stmt.ExpressionStmt(ret.getExpression().get())));
                } else {
                    System.out.println("[SKIP] Return statement without expression cannot be converted");
                    return;
                }
            } else {
                System.out.println("[SKIP] Complex switch case cannot be safely converted to switch expression");
                return;
            }
            newEntries.add(newEntry);
        }

        sexpr.setEntries(newEntries);
        ReturnStmt ret = new ReturnStmt(sexpr);
        sw.replace(ret);

        System.out.println("[ACTION] switch→return-expr: replaced SwitchStmt with return switch-expression");
    }
    
    private boolean canConvertToSwitchExpression(SwitchStmt sw) {
        // Check if all entries have simple return statements
        for (SwitchEntry entry : sw.getEntries()) {
            var statements = entry.getStatements().stream()
                .takeWhile(s -> !(s instanceof BreakStmt))
                .collect(Collectors.toList());
                
            if (statements.size() != 1 || !(statements.get(0) instanceof ReturnStmt)) {
                return false;
            }
            
            ReturnStmt ret = (ReturnStmt) statements.get(0);
            if (ret.getExpression().isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
