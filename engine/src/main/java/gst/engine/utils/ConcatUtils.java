package gst.engine.utils;

import java.util.List;

import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;

/** 
 * Flattens a PLUS‐tree of string‐literal concatenations.
 * @param expr    The root expression
 * @param parts   A list to collect the inner literal text (without quotes)
 * @return        true if every leaf was a string literal; false otherwise
 */
public class ConcatUtils {
    public static boolean gatherLiterals(Expression expr, List<String> parts) {
        if (expr instanceof BinaryExpr be && be.getOperator() == BinaryExpr.Operator.PLUS) {
            return gatherLiterals(be.getLeft(), parts)
                && gatherLiterals(be.getRight(), parts);
        } else {
            String raw = expr.toString();
            if (raw.startsWith("\"") && raw.endsWith("\"")) {
                parts.add(raw.substring(1, raw.length()-1));
                return true;
            }
            return false;
        }
    }
}
