package gst.engine.validator;

import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class TypeCompatibilityRule implements ValidationRule {
    @Override
    public List<ValidationError> apply(
            CompilationUnit cu,
            TxContext context,
            JavaSymbolSolver solver
    ) {
        List<ValidationError> errors = new ArrayList<>();

        String filePath = cu.getStorage()
                            .map(s -> s.getPath().toString())
                            .orElse("<unknown>");

        for (MethodCallExpr mc : cu.findAll(MethodCallExpr.class)) {
            // resolve method signature
            ResolvedMethodDeclaration rmd;
            try {
                rmd = mc.resolve();
            } catch (Exception e) {
                continue;
            }

            for (int i = 0; i < mc.getArguments().size(); i++) {
                var arg = mc.getArgument(i);
                if (!(arg instanceof NameExpr ne)) continue;

                String varName = ne.getNameAsString();
                if (!context.hasVarChanged(varName)) continue;

                String newType = context.getVarType(varName).orElse(null);
                // expected param type from signature
                String expectedType = rmd.getParam(i).getType().describe();

                if (newType != null && !newType.equals(expectedType)) {
                    String msg = String.format(
                        "Arg '%s' was changed to %s but method expects %s",
                        varName, newType, expectedType
                    );
                    errors.add(new ValidationError(filePath, (Node)mc, msg));
                }
            }
        }

        return errors;
    }
}
