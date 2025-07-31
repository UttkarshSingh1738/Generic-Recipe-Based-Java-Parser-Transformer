package gst.engine.validator;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.TypePatternExpr;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

/**
 * Validates that pattern variables are actually used in the then block.
 */
public class PatternVariableUsageRule implements ValidationRule {

    @Override
    public List<ValidationError> validateRecipeChanges(CompilationUnit cu, List<Node> changedNodes,
            TxContext context, JavaSymbolSolver solver, String recipeName) {
        
        List<ValidationError> errors = new ArrayList<>();
        
        // Only check InstanceOfExpr nodes that were changed by this recipe
        for (Node node : changedNodes) {
            if (node instanceof InstanceOfExpr instanceOf && instanceOf.getPattern().isPresent()) {
                validatePatternVariableUsage(instanceOf, errors);
            }
        }
        
        return errors;
    }
    
    private void validatePatternVariableUsage(InstanceOfExpr instanceOf, List<ValidationError> errors) {
        TypePatternExpr pattern = (TypePatternExpr) instanceOf.getPattern().get();
        String patternVar = pattern.getName().asString();
        
        // Find the containing if statement
        var ifStmt = instanceOf.findAncestor(IfStmt.class);
        if (ifStmt.isEmpty()) {
            return;
        }
        
        // Check if the pattern variable is used in the then block
        var thenStmt = ifStmt.get().getThenStmt();
        
        boolean isUsed = thenStmt.findAll(NameExpr.class).stream()
            .anyMatch(name -> name.getNameAsString().equals(patternVar));
            
        if (!isUsed) {
            String filePath = instanceOf.findCompilationUnit()
                .map(cu -> cu.getStorage().map(s -> s.getPath().toString()).orElse("unknown"))
                .orElse("unknown");
            errors.add(new ValidationError(
                filePath,
                instanceOf,
                "Pattern variable '" + patternVar + "' is not used in the then block. " +
                "Consider using a simple instanceof check instead."
            ));
        }
    }

    @Override
    public String getRuleName() {
        return "PatternVariableUsageRule";
    }
}
