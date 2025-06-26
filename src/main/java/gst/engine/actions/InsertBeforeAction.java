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
import gst.engine.utils.ASTUtils;

public class InsertBeforeAction implements Action {
    private final String code;

    public InsertBeforeAction(Map<String, String> params) {
        this.code = params.get("code");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        ctx.saveOriginalNode(node, node.clone());
        Statement toInsert = StaticJavaParser.parseStatement(code);

        Optional<Statement> targetOpt = ASTUtils.findEnclosingStatement(node);
        if (targetOpt.isEmpty()) return;
        Statement target = targetOpt.get();

        Optional<BlockStmt> blockOpt = target.findAncestor(BlockStmt.class);
        blockOpt.ifPresent(block -> {
            var stmts = block.getStatements();
            for (int i = 0; i < stmts.size(); i++) {
                if (stmts.get(i) == target) {
                    stmts.add(i, toInsert);
                    System.out.println("[ACTION] insertBefore: " + code);
                    break;
                }
            }
        });
    }
}
