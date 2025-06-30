package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.resolution.types.ResolvedArrayType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class ForToForEachAction implements Action {
    public ForToForEachAction(Map<String,String> params) { }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof ForStmt fs)) return;
        ctx.saveOriginalNode(fs, fs.clone());

        // loop var name
        VariableDeclarationExpr init = (VariableDeclarationExpr) fs.getInitialization().get(0);
        String idxVar = init.getVariables().get(0).getNameAsString();

        // collection expression from the compare
        Expression compare = fs.getCompare().orElseThrow();
        final Expression colExpr;
        if (compare instanceof BinaryExpr be && be.getRight() instanceof FieldAccessExpr fa
            && fa.getNameAsString().equals("length")) {
            colExpr = fa.getScope().clone();
        } else if (compare instanceof BinaryExpr be2 && be2.getRight() instanceof MethodCallExpr mc
            && mc.getNameAsString().equals("size") && mc.getScope().isPresent()) {
            colExpr = mc.getScope().get().clone();
        } else return;

        // element type
        String elemTypeName;
        try {
            ResolvedType rt = solver.calculateType(colExpr);
            if (rt.isArray()) {
                elemTypeName = ((ResolvedArrayType) rt).getComponentType().describe();
            } else if (rt.isReferenceType()) {
                var typeArgs = rt.asReferenceType().typeParametersValues();
                elemTypeName = typeArgs.isEmpty() ? "var" : typeArgs.get(0).describe();
            } else elemTypeName = "var";
        } catch (Exception ex) {
            elemTypeName = "var";
        }

        if (elemTypeName.contains("?")) {
            System.out.println("[SKIP] Could not infer a clean element type for enhanced-for: " + colExpr + " (type: " + elemTypeName + ")");
            return;
        }

        final String elemVar = (colExpr.isNameExpr() && colExpr.asNameExpr().getNameAsString().endsWith("s") && colExpr.asNameExpr().getNameAsString().length() > 1)
            ? colExpr.asNameExpr().getNameAsString().substring(0, colExpr.asNameExpr().getNameAsString().length() - 1)
            : "item";

        // build new body, replacing array[i] and list.get(i) with elemVar
        Statement origBody = fs.getBody();
        BlockStmt body = origBody.isBlockStmt()
                        ? origBody.asBlockStmt().clone()
                        : new BlockStmt(new NodeList<>(origBody.clone()));
        // replace accesses
        body.findAll(ArrayAccessExpr.class).forEach(a -> {
            if (a.getIndex().toString().equals(idxVar)
             && a.getName().toString().equals(colExpr.toString()))
                a.replace(new NameExpr(elemVar));
        });
        body.findAll(MethodCallExpr.class).forEach(mc -> {
            if (mc.getNameAsString().equals("get")
             && mc.getScope().isPresent()
             && mc.getScope().get().toString().equals(colExpr.toString())
             && mc.getArguments().size()==1
             && mc.getArgument(0).toString().equals(idxVar))
                mc.replace(new NameExpr(elemVar));
        });

        // the ForEachStmt
        VariableDeclarator vd = new VariableDeclarator(
            StaticJavaParser.parseType(elemTypeName),
            elemVar
        );
        VariableDeclarationExpr vde = new VariableDeclarationExpr(vd);
        ForEachStmt fes = new ForEachStmt(vde, colExpr, body);

        fs.replace(fes);
        System.out.println("[ACTION] Converted classic for→enhanced-for over " + colExpr + " as " + elemVar);
    }
}
