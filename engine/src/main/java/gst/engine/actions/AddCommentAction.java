package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class AddCommentAction implements Action {
    private final String comment;

    public AddCommentAction(Map<String, String> params) {
        this.comment = params.get("comment");
        if (comment == null || comment.isEmpty()) {
            throw new IllegalArgumentException("Parameter 'comment' is required for AddCommentAction.");
        }
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (comment.isBlank()) {
            return;
        }

        ctx.saveOriginalNode(node, node.clone());

        node.setLineComment(comment);

        String nodeIdentifier;
        if (node.getRange().isPresent()) {
            nodeIdentifier = "node at line " + node.getRange().get().begin.line;
        } else {
            nodeIdentifier = "node of type " + node.getClass().getSimpleName();
        }

        System.out.println("[ACTION] addComment: Added comment `// " + comment + "` to " + nodeIdentifier);
    }
}
