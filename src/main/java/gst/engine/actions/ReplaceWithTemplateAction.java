package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class ReplaceWithTemplateAction implements Action {
    private final String template;
    public ReplaceWithTemplateAction(Map<String, String> params) {
        this.template = params.get("template");
    }
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        ctx.saveOriginalNode(node, node.clone());
        Node replacement;
        if (node instanceof Statement) {
            replacement = StaticJavaParser.parseStatement(template);
        } else if (node instanceof Expression) {
            replacement = StaticJavaParser.parseExpression(template);
        } else {
            return;
        }
        node.replace(replacement);
        System.out.println("[ACTION] replaceWithTemplate: " + template);
    }
}