package gst.engine.validator;

import com.github.javaparser.ast.CompilationUnit;
import gst.engine.TxContext;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import java.util.List;

public interface ValidationRule {
    List<ValidationError> apply(
        CompilationUnit cu,
        TxContext context,
        JavaSymbolSolver solver
    );
}
