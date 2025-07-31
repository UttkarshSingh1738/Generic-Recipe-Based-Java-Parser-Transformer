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

/**
 * Validates that switch expressions are complete and well-formed.
 */
public class SwitchExpressionCompletenessRule implements ValidationRule {
    @Override
    public List<ValidationError> validateRecipeChanges(
        CompilationUnit cu,
        List<Node> changedNodes, 
        TxContext context,
        JavaSymbolSolver solver,
        String recipeName
    ) {
        List<ValidationError> errs = new ArrayList<>();
        String path = cu.getStorage()
                        .map(s -> s.getPath().toString())
                        .orElse("<unknown>");

        // Only validate switch expressions that were changed by this recipe
        for (Node node : changedNodes) {
            node.findAll(SwitchExpr.class).forEach(sexpr -> {
                for (SwitchEntry entry : sexpr.getEntries()) {
                    boolean valid = entry.getType() == SwitchEntry.Type.EXPRESSION ||
                        (!entry.getStatements().isEmpty() && (
                            entry.getStatements().get(entry.getStatements().size() - 1) instanceof ReturnStmt ||
                            entry.getStatements().get(entry.getStatements().size() - 1) instanceof ThrowStmt
                        ));
                        
                    if (!valid) {
                        Node locationNode = entry.getLabels().isNonEmpty()
                            ? entry.getLabels().get(0)
                            : entry;
                        errs.add(new ValidationError(path, locationNode, 
                            "Switch-expression entry must either produce a value or throw"));
                    }
                }
            });
        }
        return errs;
    }
    
    @Override
    public String getRuleName() {
        return "SwitchExpressionCompletenessRule";
    }
}
