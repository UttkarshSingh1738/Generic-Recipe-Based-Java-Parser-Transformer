package gst.engine.validator;

import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public interface ValidationRule {

    List<ValidationError> validateRecipeChanges(
        CompilationUnit cu,
        List<Node> changedNodes, 
        TxContext context,
        JavaSymbolSolver solver,
        String recipeName
    );
    
    String getRuleName();
}
