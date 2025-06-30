package gst.engine.validator;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.SwitchExpr;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class SwitchExpressionCompletenessRule implements ValidationRule {
    @Override
    public List<ValidationError> apply(
        CompilationUnit cu,
        TxContext context,
        JavaSymbolSolver solver
    ) {
        List<ValidationError> errs = new ArrayList<>();
        String path = cu.getStorage()
                        .map(s -> s.getPath().toString())
                        .orElse("<unknown>");

        for (SwitchExpr sexpr : cu.findAll(SwitchExpr.class)) {
            for (SwitchEntry entry : sexpr.getEntries()) {
                boolean valid;
                if (entry.getType() == SwitchEntry.Type.EXPRESSION) {
                    valid = true;
                } else { // BLOCK or other
                    var stmts = entry.getStatements();
                    valid = !stmts.isEmpty() && (
                        stmts.get(stmts.size() - 1) instanceof ReturnStmt ||
                        stmts.get(stmts.size() - 1) instanceof ThrowStmt
                    );
                }
                if (!valid) {
                    Node locationNode = entry.getLabels().isNonEmpty()
                        ? entry.getLabels().get(0)
                        : entry;
                    errs.add(new ValidationError(path, locationNode, "Switch-expression entry must either produce a value or throw"));
                }
            }
        }
        return errs;
    }
}
