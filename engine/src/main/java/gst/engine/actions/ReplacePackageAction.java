package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class ReplacePackageAction implements Action {
    private final String oldPackage;
    private final String newPackage;

    public ReplacePackageAction(Map<String, String> params) {
        this.oldPackage = params.get("oldPackage");
        this.newPackage = params.get("newPackage");
        
        if (oldPackage == null || newPackage == null) {
            throw new IllegalArgumentException("ReplacePackageAction requires 'oldPackage' and 'newPackage' parameters");
        }
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        ctx.saveOriginalNode(node, node.clone());
        
        if (node instanceof ImportDeclaration importDecl) {
            handleImportDeclaration(importDecl);
        } else if (node instanceof ClassOrInterfaceType type) {
            handleClassOrInterfaceType(type);
        } else {
            System.out.println("[WARNING] ReplacePackageAction: Unsupported node type: " + node.getClass().getSimpleName());
            return;
        }
        
        System.out.println("[ACTION] replacePackage: " + oldPackage + " → " + newPackage);
    }

    private void handleImportDeclaration(ImportDeclaration importDecl) {
        String importName = importDecl.getNameAsString();
        if (importName.startsWith(oldPackage)) {
            String newImportName = importName.replace(oldPackage, newPackage);
            importDecl.setName(newImportName);
        }
    }

    private void handleClassOrInterfaceType(ClassOrInterfaceType type) {
        // Handle fully qualified type names directly in the name
        String typeName = type.getNameAsString();
        if (typeName.startsWith(oldPackage)) {
            String newTypeName = typeName.replace(oldPackage, newPackage);
            type.setName(newTypeName);
            return;
        }
        
        // Handle scope (package qualification) for types like javax.security.cert.X509Certificate
        type.getScope().ifPresent(scope -> {
            String scopeName = scope.asString();
            if (scopeName.startsWith(oldPackage)) {
                String newScopeName = scopeName.replace(oldPackage, newPackage);
                ClassOrInterfaceType newScope = parseClassOrInterfaceType(newScopeName);
                type.setScope(newScope);
            }
        });
    }

    private ClassOrInterfaceType parseClassOrInterfaceType(String qualifiedName) {
        String[] parts = qualifiedName.split("\\.");
        if (parts.length == 1) {
            return new ClassOrInterfaceType(null, parts[0]);
        }
        
        ClassOrInterfaceType result = new ClassOrInterfaceType(null, parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result = new ClassOrInterfaceType(result, parts[i]);
        }
        return result;
    }
}
