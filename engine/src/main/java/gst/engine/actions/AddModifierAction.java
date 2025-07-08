package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithModifiers;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class AddModifierAction implements Action {
    private final String modifier; // e.g. "public", "static", etc
    public AddModifierAction(Map<String,String> params) {
        this.modifier = params.get("modifier");
    }
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof NodeWithModifiers<?> nm)) return;
        ctx.saveOriginalNode(node, node.clone());
        nm.addModifier(com.github.javaparser.ast.Modifier.Keyword.valueOf(modifier.toUpperCase()));
        System.out.println("[ACTION] addModifier: " + modifier);
    }
}
