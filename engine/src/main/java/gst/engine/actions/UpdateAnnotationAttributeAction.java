package gst.engine.actions;

import java.util.Map;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.expr.MemberValuePair;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

public class UpdateAnnotationAttributeAction implements Action {

    private final String annotation;
    private final String oldAttribute;
    private final String newAttribute;
    private final String newValue;

    public UpdateAnnotationAttributeAction(Map<String, String> p) {
        this.annotation = p.get("annotation");
        this.oldAttribute = p.get("oldAttribute");
        this.newAttribute = p.getOrDefault("newAttribute", oldAttribute);
        this.newValue = p.get("newValue");
    }

    @Override
    public void apply(Node node, CompilationUnit cu, TxContext ctx, JavaSymbolSolver solver) {
        if (!(node instanceof NodeWithAnnotations<?> nwa)) {
            return;
        }
        nwa.getAnnotationByName(annotation).ifPresent(a -> {
            if (a.isNormalAnnotationExpr()) {
                ctx.saveOriginalNode(node, node.clone());
                NormalAnnotationExpr nae = a.asNormalAnnotationExpr();

                for (MemberValuePair pair : nae.getPairs()) {
                    if (pair.getNameAsString().equals(oldAttribute)) {
                        pair.setName(newAttribute);
                        pair.setValue(StaticJavaParser.parseExpression(newValue));
                        System.out.println("[ACTION] updateAnnotationAttribute: @"
                                + annotation + "("
                                + oldAttribute + "→" + newAttribute
                                + " = " + newValue + ")");
                        return;
                    }
                }
                MemberValuePair added = new MemberValuePair(
                        newAttribute, StaticJavaParser.parseExpression(newValue)
                );
                nae.getPairs().add(added);
                System.out.println("[ACTION] addedAnnotationAttribute: @"
                        + annotation + "(" + newAttribute + " = " + newValue + ")");
            }
        });
    }
}
