package gst.engine.actions;

import java.util.Map;
import java.util.Optional;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class InsertBeforeAction implements Action {
    private final String code;
    public InsertBeforeAction(Map<String, String> params) {
        this.code = params.get("code");
    }
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        ctx.saveOriginalNode(node, node.clone());
        Statement stmt = StaticJavaParser.parseStatement(code);
        Optional<BlockStmt> parentBlock = node.findAncestor(BlockStmt.class);
        parentBlock.ifPresent(block -> {
            var list = block.getStatements();
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) == node) {
                    list.add(i, stmt);
                    System.out.println("[ACTION] insertBefore: " + code);
                    break;
                }
            }
        });
    }
}