package gst.engine.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;
import gst.engine.utils.ConcatUtils;

public class CollapseLiteralConcatAction implements Action {

    private final boolean stringsWithoutNewlines;

    public CollapseLiteralConcatAction(Map<String, String> params) {
        this.stringsWithoutNewlines = params.containsKey("stringsWithoutNewlines")
                ? Boolean.parseBoolean(params.get("stringsWithoutNewlines"))
                : true;
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof BinaryExpr top)
                || top.getOperator() != BinaryExpr.Operator.PLUS) {
            return;
        }

        List<String> parts = new ArrayList<>();
        if (!ConcatUtils.gatherLiterals(top, parts)) {
            return;
        }

        String fullText = String.join("", parts);
        boolean containsNewlines = fullText.contains("\\n") || fullText.contains("\n");

        if (!containsNewlines && !stringsWithoutNewlines) {
            return;
        }

        ctx.saveOriginalNode(top, top.clone());

        String textBlockContent = formatForTextBlock(fullText);
        top.replace(new TextBlockLiteralExpr(textBlockContent));

        System.out.println("[ACTION] collapse literal concat → TEXT_BLOCK");
    }

    private String formatForTextBlock(String content) {
        String formatted = content.replace("\\n", "\n")
                .replace("\\t", "\t");

        boolean endsWithNewline = content.endsWith("\\n") || content.endsWith("\n");

        if (!endsWithNewline && formatted.contains("\n")) {
            String[] lines = formatted.split("\n", -1);
            StringBuilder result = new StringBuilder();

            for (int i = 0; i < lines.length; i++) {
                result.append(lines[i]);

                if (i < lines.length - 1) {
                    if (lines[i].trim().isEmpty() && i < lines.length - 2) {
                        result.append("\n");
                    } else if (i < lines.length - 1) {
                        result.append(" \\\n");
                    }
                }
            }
            return result.toString();
        }

        return formatted;
    }
}
