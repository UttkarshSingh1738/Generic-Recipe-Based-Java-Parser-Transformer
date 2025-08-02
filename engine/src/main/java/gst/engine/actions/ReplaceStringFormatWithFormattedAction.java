package gst.engine.actions;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class ReplaceStringFormatWithFormattedAction implements Action {
    private final boolean parenthesize;

    public ReplaceStringFormatWithFormattedAction(Map<String, String> params) {
        this.parenthesize = params.containsKey("parenthesizeFirstArg")
            ? Boolean.parseBoolean(params.get("parenthesizeFirstArg"))
            : true;
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof MethodCallExpr mc)) return;
        if (!mc.getNameAsString().equals("format")) return;
        if (mc.getScope().filter(s -> s.toString().equals("String")).isEmpty()) return;
        var args = mc.getArguments();
        if (args.isEmpty()) return;

        Expression formatString = args.get(0);
        
        boolean isStringLiteral = formatString.isStringLiteralExpr();
        boolean isSimpleVariable = formatString.isNameExpr();
        boolean hasPlaceholders = containsFormatPlaceholders(formatString);
        boolean hasFormatArguments = args.size() > 1;

        if (isStringLiteral && !hasPlaceholders) {return;}
        if (isSimpleVariable && !hasFormatArguments) {return;}
        
        Expression clonedFormatString = formatString.clone();
        
        // Handle parentheses for complex expressions
        if (parenthesize && !(formatString.isNameExpr() || formatString.isLiteralStringValueExpr())) {
            clonedFormatString = new EnclosedExpr(clonedFormatString);
        }

        List<Expression> rest = args.subList(1, args.size())
            .stream().map(Expression::clone).collect(Collectors.toList());

        MethodCallExpr formatted = new MethodCallExpr(
            clonedFormatString, "formatted", new com.github.javaparser.ast.NodeList<>(rest));

        ctx.saveOriginalNode(mc, mc.clone());
        mc.replace(formatted);
        System.out.println("[ACTION] replaceStringFormatWithFormatted");
    }
    
    private boolean containsFormatPlaceholders(Expression expr) {
        if (expr.isStringLiteralExpr()) {
            String value = expr.asStringLiteralExpr().getValue();
            return value.contains("%s") || value.contains("%d") || value.contains("%f") || 
                value.contains("%c") || value.contains("%b") || value.matches(".*%\\d*[sdfc].*");
        }
        return false;
    }
}
