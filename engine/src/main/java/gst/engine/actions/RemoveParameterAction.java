package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RemoveParameterAction implements Action {
    private final String paramName;
    public RemoveParameterAction(Map<String,String> params) {
        this.paramName = params.get("name");
    }
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof MethodDeclaration md)) return;
        ctx.saveOriginalNode(md, md.clone());
        md.getParameters().stream()
          .filter(p -> p.getNameAsString().equals(paramName))
          .findFirst()
          .ifPresent(p -> {
              p.remove();
              System.out.println("[ACTION] removeParameter '" + paramName + "'");
          });
    }
}
