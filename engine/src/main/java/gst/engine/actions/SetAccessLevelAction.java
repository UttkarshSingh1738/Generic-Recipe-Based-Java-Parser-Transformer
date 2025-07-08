package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class SetAccessLevelAction implements Action {

    private final String level; // "public","protected","private","package"

    public SetAccessLevelAction(Map<String, String> params) {
        this.level = params.get("level");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof NodeWithModifiers<?> nm)) {
            return;
        }
        ctx.saveOriginalNode(node, node.clone());
        nm.getModifiers().removeIf(m -> {
            Modifier.Keyword k = m.getKeyword();
            return k == Modifier.Keyword.PUBLIC
                    || k == Modifier.Keyword.PROTECTED
                    || k == Modifier.Keyword.PRIVATE;
        });
        switch (level) {
            case "public" ->
                nm.addModifier(Modifier.Keyword.PUBLIC);
            case "protected" ->
                nm.addModifier(Modifier.Keyword.PROTECTED);
            case "private" ->
                nm.addModifier(Modifier.Keyword.PRIVATE);
            case "package" -> { /* no modifier */ }
        }
        System.out.println("[ACTION] setAccessLevel: " + level);
    }
}
