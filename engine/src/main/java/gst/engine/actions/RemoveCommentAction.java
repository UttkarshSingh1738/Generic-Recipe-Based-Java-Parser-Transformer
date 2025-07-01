package gst.engine.actions;

import java.util.Map;
import java.util.function.Predicate;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RemoveCommentAction implements Action {
    private final String pattern;

    public RemoveCommentAction(Map<String,String> params) {
        this.pattern = params.get("pattern");
    }

    @Override
    public void apply(Node node,
                      CompilationUnit cu,
                      TxContext ctx,
                      JavaSymbolSolver solver) {
        ctx.saveOriginalNode(node, node.clone());

        Predicate<Comment> toRemove = c -> {
            if (pattern == null || pattern.isBlank()) return true;
            return c.getContent().contains(pattern);
        };

        // remove comment attached directly to this node
        node.getComment()
            .filter(toRemove)
            .ifPresent(Comment::remove);

        // remove all comments in the subtree
        node.getAllContainedComments().stream()
            .filter(toRemove)
            .forEach(Comment::remove);

        System.out.println("[ACTION] removeComment" +
           (pattern != null && !pattern.isBlank()
                ? " matching \"" + pattern + "\"" 
                : " (all)"));
    }
}