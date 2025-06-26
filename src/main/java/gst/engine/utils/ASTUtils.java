package gst.engine.utils;

import java.util.Optional;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;

/**
 * Utility methods for AST transformations.
 */
public class ASTUtils {

    /**
     * Finds the nearest enclosing Statement for any given Node.
     * If the node itself is a Statement (but not a BlockStmt), returns it.
     * Otherwise climbs the parent chain until a Statement is found.
     */
    public static Optional<Statement> findEnclosingStatement(Node node) {
        if (node instanceof Statement stmt && !(stmt instanceof BlockStmt)) {
            return Optional.of(stmt);
        }
        return node.findAncestor(Statement.class)
                   .filter(s -> !(s instanceof BlockStmt));
    }
}
