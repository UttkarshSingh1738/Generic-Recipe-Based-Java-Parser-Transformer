package gst.engine.actions;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.InstanceOfExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.TypePatternExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;
import gst.engine.utils.VariableNameGenerator;

public class InstanceOfToPatternAction implements Action {

    InstanceOfToPatternAction(Map<String, String> stringParams) {}
    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof InstanceOfExpr ioe)) return;
        if (ioe.getPattern().isPresent()) return;

        Expression testedExpr = ioe.getExpression().clone();
        var testedType = ioe.getType().clone();

        String varName = VariableNameGenerator.generatePatternVariableName(testedType);
        
        if (varName == null) {
            System.out.println("[SKIP] Cannot generate safe variable name for type: " + testedType);
            return;
        }

        ctx.saveOriginalNode(ioe, ioe.clone());
        ioe.setPattern(new TypePatternExpr(
                new com.github.javaparser.ast.NodeList<>(),
                testedType,
                new com.github.javaparser.ast.expr.SimpleName(varName)
        ));
        System.out.println("[ACTION] instanceOfToPattern: inserted pattern var '" + varName + "'");

        @SuppressWarnings("unchecked")
        Optional<IfStmt> ifOpt = ioe.findAncestor(IfStmt.class);
        if (ifOpt.isEmpty()) return;
        IfStmt ifStmt = ifOpt.get();
        BlockStmt thenBlock;
        if (ifStmt.getThenStmt().isBlockStmt()) {
            thenBlock = ifStmt.getThenStmt().asBlockStmt();
        } else {
            thenBlock = new BlockStmt().addStatement(ifStmt.getThenStmt().clone());
            ifStmt.setThenStmt(thenBlock);
        }

        // Remove redundant cast declarations
        List<VariableDeclarationExpr> toRemove = thenBlock.findAll(VariableDeclarationExpr.class, vde -> {
            if (vde.getVariables().size() != 1) return false;
            var vd = vde.getVariables().get(0);
            if (vd.getInitializer().filter(i -> i instanceof CastExpr).isEmpty()) return false;
            CastExpr ce = (CastExpr) vd.getInitializer().get();
            if (!ce.getExpression().equals(testedExpr)) return false;
            if (!(ce.getType().isClassOrInterfaceType() && testedType.isClassOrInterfaceType())) return false;
            return ce.getType().asClassOrInterfaceType().getNameAsString()
                    .equals(testedType.asClassOrInterfaceType().getNameAsString());
        });
        for (VariableDeclarationExpr vde : toRemove) {
            Optional<Node> parent = vde.getParentNode();
            if (parent.isPresent() && parent.get() instanceof Statement stmt) {
                ctx.saveOriginalNode(stmt, stmt.clone());
                thenBlock.getStatements().remove(stmt);
                String oldName = vde.getVariables().get(0).getNameAsString();
                System.out.println("[ACTION] instanceOfToPattern: removed old cast‑decl '" + oldName + "'");
                thenBlock.accept(new VoidVisitorAdapter<Void>() {
                    @Override
                    public void visit(NameExpr ne, Void arg) {
                        super.visit(ne, arg);
                        if (ne.getNameAsString().equals(oldName)) {
                            ne.setName(varName);
                        }
                    }
                }, null);
            }
        }

        // Replace redundant cast expressions
        thenBlock.findAll(CastExpr.class, ce -> {
            if (!ce.getExpression().equals(testedExpr)) return false;
            if (!(ce.getType().isClassOrInterfaceType() && testedType.isClassOrInterfaceType())) return false;
            return ce.getType().asClassOrInterfaceType().getNameAsString()
                    .equals(testedType.asClassOrInterfaceType().getNameAsString());
        }).forEach(ce -> {
            ctx.saveOriginalNode(ce, ce.clone());
            ce.replace(new NameExpr(varName));
            System.out.println("[ACTION] instanceOfToPattern: replaced cast‑expr at " + ce.getRange().orElse(null));
        });
    }
}
