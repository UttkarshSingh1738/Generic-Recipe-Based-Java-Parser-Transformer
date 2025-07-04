package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RemoveModifierAction implements Action {
  private final String modifier;
  public RemoveModifierAction(Map<String,String> p) { modifier = p.get("modifier"); }
  @Override
  public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
    if (node instanceof NodeWithModifiers<?> nwm) {
      ctx.saveOriginalNode(node, node.clone());
      nwm.getModifiers().removeIf(m -> m.getKeyword().asString().equals(modifier));
    }
  }
}
