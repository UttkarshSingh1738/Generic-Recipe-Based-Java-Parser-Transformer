package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class RenameClassAction implements Action {

    private final String newName;

    public RenameClassAction(Map<String, String> params) {
        this.newName = params.get("newName");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof ClassOrInterfaceDeclaration cd)) {
            return;
        }
        String oldName = cd.getNameAsString();

        ctx.saveOriginalNode(cd, cd.clone());

        cd.setName(newName);
        System.out.println("[ACTION] renameClass: " + oldName + " → " + newName);

        cu.findAll(ClassOrInterfaceType.class)
                .stream()
                .filter(cit -> cit.getNameAsString().equals(oldName))
                .forEach(cit -> cit.setName(newName));
    }
}
