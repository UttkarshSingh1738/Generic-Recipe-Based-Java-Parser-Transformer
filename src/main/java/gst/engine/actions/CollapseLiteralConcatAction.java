package gst.engine.actions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.TextBlockLiteralExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;
import gst.engine.utils.ConcatUtils;

public class CollapseLiteralConcatAction implements Action {
  // collapseStyle: "TEXT_BLOCK" or "SINGLE_LITERAL"
  private final String collapseStyle;

  public CollapseLiteralConcatAction(Map<String,String> params) {
    this.collapseStyle = params.getOrDefault("collapseStyle","SINGLE_LITERAL");
  }

  @Override
  public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
    if (!(node instanceof BinaryExpr top)
     || top.getOperator() != BinaryExpr.Operator.PLUS) return;
    ctx.saveOriginalNode(top, top.clone());

    List<String> parts = new ArrayList<>();
    if (!ConcatUtils.gatherLiterals(top, parts)) return;

    // decide style
    if ("TEXT_BLOCK".equals(collapseStyle)) {
      String joined = String.join("", parts)
                          .replace("\\n","\n")
                          ;
      top.replace(new TextBlockLiteralExpr(joined));
    } else {
      // SINGLE_LITERAL: re-escape newlines and re-emit one big literal
      String rejoined = parts.stream()
         .map(p -> p.replace("\n","\\n").replace("\t","\\t"))
         .collect(Collectors.joining());
      top.replace(new StringLiteralExpr(rejoined));
    }
    System.out.println("[ACTION] collapse literal concat → " + collapseStyle);
  }
}

