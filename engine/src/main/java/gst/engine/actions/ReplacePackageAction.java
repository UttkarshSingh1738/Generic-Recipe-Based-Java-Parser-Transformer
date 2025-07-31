package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
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
        } else if (node instanceof FieldAccessExpr fieldAccess) {
            handleFieldAccessExpr(fieldAccess);
        } else if (node instanceof MethodCallExpr methodCall) {
            handleMethodCallExpr(methodCall);
        } else if (node instanceof ObjectCreationExpr objectCreation) {
            handleObjectCreationExpr(objectCreation);
        } else if (node instanceof NameExpr nameExpr) {
            handleNameExpr(nameExpr);
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

    private void handleFieldAccessExpr(FieldAccessExpr fieldAccess) {
        // Handle cases like com.sun.net.ssl.SSLContext.getInstance()
        String fullExpression = fieldAccess.toString();
        if (fullExpression.startsWith(oldPackage)) {
            // Replace the scope of the field access
            if (fieldAccess.getScope().toString().startsWith(oldPackage)) {
                String newScopeStr = fieldAccess.getScope().toString().replace(oldPackage, newPackage);
                NameExpr newScope = new NameExpr(newScopeStr);
                fieldAccess.setScope(newScope);
            }
        }
        
        // Also recursively handle scope if it's another FieldAccessExpr
        if (fieldAccess.getScope() instanceof FieldAccessExpr) {
            FieldAccessExpr scopeFieldAccess = (FieldAccessExpr) fieldAccess.getScope();
            handleFieldAccessExpr(scopeFieldAccess);
        }
    }

    private void handleMethodCallExpr(MethodCallExpr methodCall) {
        // Handle method calls on qualified types like com.sun.net.ssl.SSLContext.getInstance()
        methodCall.getScope().ifPresent(scope -> {
            if (scope instanceof FieldAccessExpr) {
                FieldAccessExpr fieldAccess = (FieldAccessExpr) scope;
                handleFieldAccessExpr(fieldAccess);
            } else if (scope instanceof NameExpr) {
                NameExpr nameExpr = (NameExpr) scope;
                handleNameExpr(nameExpr);
            }
        });
    }

    private void handleObjectCreationExpr(ObjectCreationExpr objectCreation) {
        // Handle object creation with qualified types like new com.sun.net.ssl.SSLContext()
        ClassOrInterfaceType type = objectCreation.getType();
        handleClassOrInterfaceType(type);
    }

    private void handleNameExpr(NameExpr nameExpr) {
        // Handle simple qualified names like com.sun.net.ssl.SSLContext
        String name = nameExpr.getNameAsString();
        if (name.startsWith(oldPackage)) {
            String newName = name.replace(oldPackage, newPackage);
            nameExpr.setName(newName);
        }
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
