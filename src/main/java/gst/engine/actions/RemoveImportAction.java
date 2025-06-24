package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RemoveImportAction implements Action {
    private final String name;
    public RemoveImportAction(Map<String, String> params) {
        this.name = params.get("name");
    }
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        ctx.saveOriginalNode(cu, cu.clone());
        cu.getImports().removeIf(i -> i.getNameAsString().equals(name));
        System.out.println("[ACTION] removeImport: " + name);
    }
}