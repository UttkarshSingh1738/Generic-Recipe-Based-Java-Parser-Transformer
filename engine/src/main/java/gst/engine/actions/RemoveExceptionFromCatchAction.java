package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.type.UnionType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RemoveExceptionFromCatchAction implements Action {
    private final String exceptionToRemove;

    public RemoveExceptionFromCatchAction(Map<String,String> params) {
        this.exceptionToRemove = params.get("exception");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof CatchClause cc)) return;
        ctx.saveOriginalNode(cc, cc.clone());

        var param = cc.getParameter();
        var type  = param.getType();

        if (type.isUnionType()) {
            var union = type.asUnionType();
            union.getElements().removeIf(t -> t.asString().equals(exceptionToRemove));

            var remaining = union.getElements();
            if (remaining.isEmpty()) {
                param.setType(StaticJavaParser.parseType("Exception"));
            } else if (remaining.size() == 1) {
                param.setType(remaining.get(0));
            } else {
                param.setType(new UnionType(remaining));
            }
        } else if (type.asString().equals(exceptionToRemove)) {
            param.setType(StaticJavaParser.parseType("Exception"));
        }

        System.out.println("[ACTION] removeExceptionFromCatch: " + exceptionToRemove);
    }
}
