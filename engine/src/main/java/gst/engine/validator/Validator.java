package gst.engine.validator;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class Validator {
    private static final List<ValidationRule> RULES = List.of(
        new TypeCompatibilityRule(),
        new OverrideRule(),
        new SwitchExpressionCompletenessRule()
    );

     // Returns a list of all validation errors found after mutation.
    public static List<ValidationError> run(
            List<CompilationUnit> units,
            TxContext context,
            JavaSymbolSolver solver
    ) {
        List<ValidationError> allErrors = new ArrayList<>();
        for (CompilationUnit cu : units) {
            for (ValidationRule rule : RULES) {
                allErrors.addAll(rule.apply(cu, context, solver));
            }
        }
        return allErrors;
    }
}
