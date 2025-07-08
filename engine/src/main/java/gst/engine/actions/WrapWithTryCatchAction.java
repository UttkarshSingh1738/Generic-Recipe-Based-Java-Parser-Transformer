package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class WrapWithTryCatchAction implements Action {

    private final String exceptionType, exceptionVar;

    public WrapWithTryCatchAction(Map<String, String> params) {
        String t = params.get("exceptionType");
        String v = params.get("exceptionVar");
        this.exceptionType = (t != null && !t.isBlank()) ? t : "Exception";
        this.exceptionVar  = (v != null && !v.isBlank()) ? v : "e";
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof Statement stmt)) {
            return;
        }
        ctx.saveOriginalNode(node, node.clone());
        TryStmt ts = new TryStmt();
        ts.setTryBlock(new BlockStmt().addStatement(stmt.clone()));
        CatchClause cc = new CatchClause();
        cc.setParameter(StaticJavaParser.parseParameter(exceptionType + " " + exceptionVar));
        cc.setBody(new BlockStmt());
        ts.setCatchClauses(new NodeList<>(cc));
        stmt.replace(ts);
        System.out.println("[ACTION] wrapWithTryCatch: try{" + stmt + "} catch("
                + exceptionType + " " + exceptionVar + ")");
    }
}
