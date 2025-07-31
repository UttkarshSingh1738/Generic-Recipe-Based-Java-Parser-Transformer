package gst.engine.validator;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

/**
 * Validates enhanced for loop transformations and usage patterns.
 */
public class EnhancedForUsageRule implements ValidationRule {
    
    @Override
    public List<ValidationError> validateRecipeChanges(
        CompilationUnit cu,
        List<Node> changedNodes, 
        TxContext context,
        JavaSymbolSolver solver,
        String recipeName
    ) {
        List<ValidationError> errors = new ArrayList<>();
        
        for (Node node : changedNodes) {
            if (node instanceof ForStmt) {
                validateForStmtConversion((ForStmt) node, errors);
            } else if (node instanceof ForEachStmt) {
                validateEnhancedForUsage((ForEachStmt) node, errors);
            }
        }
        
        return errors;
    }
    
    @Override
    public String getRuleName() {
        return "EnhancedForUsageRule";
    }
    
    private void validateForStmtConversion(ForStmt forStmt, List<ValidationError> errors) {
        if (forStmt.getInitialization().isEmpty() || forStmt.getCompare().isEmpty()) {
            return;
        }
        
        String indexVar = extractIndexVariable(forStmt);
        if (indexVar != null) {
            boolean indexUsedForOtherPurpose = forStmt.getBody().findAll(NameExpr.class).stream()
                .filter(name -> name.getNameAsString().equals(indexVar))
                .count() > 1;
                
            if (indexUsedForOtherPurpose) {
                String filePath = forStmt.findCompilationUnit()
                    .map(cu -> cu.getStorage().map(s -> s.getPath().toString()).orElse("unknown"))
                    .orElse("unknown");
                errors.add(new ValidationError(
                    filePath,
                    forStmt,
                    "Index variable '" + indexVar + "' is used for purposes other than iteration. " +
                    "Enhanced for loop conversion may change semantics."
                ));
            }
        }
    }
    
    private void validateEnhancedForUsage(ForEachStmt forEach, List<ValidationError> errors) {
        String iterVar = forEach.getVariable().getVariable(0).getNameAsString();
        
        boolean varUsed = forEach.getBody().findAll(NameExpr.class).stream()
            .anyMatch(name -> name.getNameAsString().equals(iterVar));
            
        if (!varUsed) {
            String filePath = forEach.findCompilationUnit()
                .map(cu -> cu.getStorage().map(s -> s.getPath().toString()).orElse("unknown"))
                .orElse("unknown");
            errors.add(new ValidationError(
                filePath,
                forEach,
                "Enhanced for loop variable '" + iterVar + "' is never used. " +
                "Consider using a simple iteration or different approach."
            ));
        }
    }
    
    private String extractIndexVariable(ForStmt forStmt) {
        if (forStmt.getInitialization().size() == 1 && 
            forStmt.getInitialization().get(0) instanceof VariableDeclarationExpr) {
            
            VariableDeclarationExpr initExpr = (VariableDeclarationExpr) forStmt.getInitialization().get(0);
            if (initExpr.getVariables().size() == 1) {
                return initExpr.getVariable(0).getNameAsString();
            }
        }
        return null;
    }
}
