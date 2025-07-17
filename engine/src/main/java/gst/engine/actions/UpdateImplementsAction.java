package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class UpdateImplementsAction implements Action {
    private final String toAdd, toRemove;

    public UpdateImplementsAction(Map<String,String> params) {
        this.toAdd    = params.get("add");
        this.toRemove = params.get("remove");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof ClassOrInterfaceDeclaration cd)) return;
        ctx.saveOriginalNode(cd, cd.clone());

        cd.getImplementedTypes().removeIf(t ->
            t.getNameAsString().equals(toRemove)
        );
        boolean present = cd.getImplementedTypes().stream()
            .anyMatch(t -> t.getNameAsString().equals(toAdd));
        if (!present) {
            cd.addImplementedType(toAdd);
        }

        System.out.println("[ACTION] updateImplements: added '"
            + toAdd + "', removed '" + toRemove + "'");
    }
}
