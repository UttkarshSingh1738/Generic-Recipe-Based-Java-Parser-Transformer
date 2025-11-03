package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class ClearInitializerAction implements Action {
  public ClearInitializerAction(Map<String, String> params) {}
  @Override
  public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
    ctx.saveOriginalNode(node, node.clone());
    if (node instanceof FieldDeclaration fd) {
      fd.getVariables().forEach(v -> v.removeInitializer());
      System.out.println("[ACTION] clearInitializer on field");
    } else if (node instanceof VariableDeclarationExpr vde) {
      vde.getVariables().forEach(v -> v.removeInitializer());
      System.out.println("[ACTION] clearInitializer on variable");
    }
  }
}
