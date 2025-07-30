package gst.engine.actions;

import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class ChangeMethodTargetToStaticAction implements Action {
    private final String methodPattern;
    private final String targetType;

    public ChangeMethodTargetToStaticAction(Map<String, String> params) {
        this.methodPattern = params.get("methodPattern");
        this.targetType = params.get("targetType");
        
        if (methodPattern == null || targetType == null) {
            throw new IllegalArgumentException("methodPattern and targetType are required");
        }
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof MethodCallExpr mc)) return;
        
        Optional<Expression> scope = mc.getScope();
        if (scope.isEmpty()) return;
        
        // Skip if already static
        if (scope.get() instanceof NameExpr ne && 
            ne.getNameAsString().equals(getSimpleTypeName(targetType))) {
            return;
        }
        
        ctx.saveOriginalNode(mc, mc.clone());
        
        addImportIfNeeded(cu, targetType);
        
        String simpleTargetType = getSimpleTypeName(targetType);
        mc.setScope(new NameExpr(simpleTargetType));
        
        System.out.println("[ACTION] changeMethodTargetToStatic: " + targetType + "." + mc.getName());
    }
    
    private void addImportIfNeeded(CompilationUnit cu, String fullyQualifiedType) {
        boolean importExists = cu.getImports().stream()
            .anyMatch(imp -> imp.getNameAsString().equals(fullyQualifiedType));
        
        if (!importExists) {
            cu.addImport(fullyQualifiedType);
        }
    }
    
    private String getSimpleTypeName(String fullyQualifiedName) {
        int lastDot = fullyQualifiedName.lastIndexOf('.');
        return lastDot >= 0 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName;
    }
}
