package gst.engine.actions;

import gst.engine.TxContext;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import java.util.List;
import java.util.Map;

public class WrapArgumentAction implements Action {
    private final String template;
    private final List<String> addImports;

    @SuppressWarnings("unchecked")
    public WrapArgumentAction(Map<String, Object> params) {
        this.template   = (String) params.get("template");
        this.addImports = (List<String>) params.get("addImports");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof MethodCallExpr mc)) return;

        // Wrap *each* argument in the template (uses $ARG$ placeholder)
        for (int i = 0; i < mc.getArguments().size(); i++) {
            Expression arg = mc.getArgument(i);
            String wrapped = template.replace("$ARG$", arg.toString());
            mc.setArgument(i, StaticJavaParser.parseExpression(wrapped));
        }

        // Add any required imports
        if (addImports != null) {
            for (String imp : addImports) {
                boolean present = cu.getImports()
                    .stream()
                    .anyMatch(id -> id.getNameAsString().equals(imp));
                if (!present) {
                    cu.addImport(imp);
                }
            }
        }
    }
}
