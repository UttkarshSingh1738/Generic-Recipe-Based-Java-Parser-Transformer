package gst.engine.actions;

import java.util.LinkedHashMap;
import java.util.Map;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class MigrateAnnotationAction implements Action {
    private final String from;
    private final String to;
    private final Map<String,String> attrMap;

    @SuppressWarnings("unchecked")
    public MigrateAnnotationAction(Map<String, Object> params) {
        this.from = (String) params.get("from");
        this.to   = (String) params.get("to");
        var raw  = (Map<String,Object>) params.get("attributeMap");
        if (raw == null) {
            this.attrMap = Map.of();
        } else {
            this.attrMap = new LinkedHashMap<>();
            raw.forEach((k,v) -> this.attrMap.put(k, v.toString()));
        }
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof NodeWithAnnotations<?> nwa)) return;

        AnnotationExpr fromAnn = nwa.getAnnotationByName(from).orElse(null);
        if (fromAnn == null) return;

        Map<String,MemberValuePair> oldPairs = new LinkedHashMap<>();
        if (fromAnn instanceof SingleMemberAnnotationExpr sma) {
            MemberValuePair p = new MemberValuePair("value", sma.getMemberValue().clone());
            oldPairs.put("value", p);
        } else if (fromAnn.isNormalAnnotationExpr()) {
            for (MemberValuePair p : fromAnn.asNormalAnnotationExpr().getPairs()) {
                oldPairs.put(p.getNameAsString(), p);
            }
        }

        ctx.saveOriginalNode(node, node.clone());

        nwa.getAnnotations().removeIf(a -> a.equals(fromAnn));

        // get or create the "to" annotation as a NormalAnnotationExpr
        NormalAnnotationExpr target;
        var existing = nwa.getAnnotationByName(to);
        if (existing.isPresent() && existing.get().isNormalAnnotationExpr()) {
            target = existing.get().asNormalAnnotationExpr();
        } else {
            target = new NormalAnnotationExpr();
            target.setName(to);
            target.setPairs(new NodeList<>());
            nwa.addAnnotation(target);
        }

        for (var entry : attrMap.entrySet()) {
            String oldName = entry.getKey();
            String newName = entry.getValue();
            MemberValuePair oldPair = oldPairs.get(oldName);
            if (oldPair != null) {
                target.getPairs().removeIf(p -> p.getNameAsString().equals(newName));
                // clone the old expression node
                var cloned = oldPair.clone();
                cloned.setName(newName);
                target.getPairs().add(cloned);
            }
        }

        System.out.println("[ACTION] migrateAnnotation: @" +
            from + " → @" + to +
            " (" + attrMap + ")");
    }
}
