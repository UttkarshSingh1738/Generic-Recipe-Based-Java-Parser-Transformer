package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class AddImportAction implements Action {
    private final String name;
    public AddImportAction(Map<String, String> params) {
        this.name = params.get("name");
    }
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        ctx.saveOriginalNode(cu, cu.clone());
        boolean present = cu.getImports().stream()
            .anyMatch(i -> i.getNameAsString().equals(name));
        if (!present) {
            cu.addImport(name);
            System.out.println("[ACTION] addImport: " + name);
        }
    }
}