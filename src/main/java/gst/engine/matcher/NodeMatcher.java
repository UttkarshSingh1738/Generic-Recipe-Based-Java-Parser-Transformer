package gst.engine.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import gst.api.Match;
import gst.engine.utils.ConcatUtils;

public class NodeMatcher {

    /** Find all candidate nodes of the given type in this CU. */
    public static List<Node> findCandidates(Node root, String nodeType) {
        return switch (nodeType) {
          case "ObjectCreationExpr"      -> root.findAll(ObjectCreationExpr.class).stream().map(n->(Node)n).collect(Collectors.toList());
          case "VariableDeclarationExpr" -> root.findAll(VariableDeclarationExpr.class).stream().map(n->(Node)n).collect(Collectors.toList());
          case "MethodCallExpr"          -> root.findAll(MethodCallExpr.class).stream().map(n->(Node)n).collect(Collectors.toList());
          case "FieldAccessExpr"         -> root.findAll(FieldAccessExpr.class).stream().map(n->(Node)n).collect(Collectors.toList());
          case "AnnotationExpr"          -> root.findAll(AnnotationExpr.class).stream().map(n->(Node)n).collect(Collectors.toList());
          case "ImportDeclaration"       -> root.findAll(ImportDeclaration.class).stream().map(n->(Node)n).collect(Collectors.toList());
          case "NameExpr"                -> root.findAll(NameExpr.class).stream().map(n->(Node)n).collect(Collectors.toList());
          case "Parameter"               -> root.findAll(Parameter.class).stream().map(n->(Node)n).collect(Collectors.toList());
          case "ClassOrInterfaceType"    -> root.findAll(ClassOrInterfaceType.class).stream().map(n->(Node)n).collect(Collectors.toList());
          case "SwitchStmt"              -> root.findAll(SwitchStmt.class).stream().map(n->(Node)n).collect(Collectors.toList());
          case "BinaryExpr"              -> root.findAll(BinaryExpr.class).stream().map(n->(Node)n).collect(Collectors.toList());
          default -> List.of();
        };
    }

    /** Core matching logic, returns true if node satisfies every non-null field in m. */
    public static boolean matches(
            Node node,
            Match m,
            CombinedTypeSolver typeSolver
    ) {
        // 1) nodeType sanity
        if (!node.getClass().getSimpleName().equals(m.nodeType)) {
            return false;
        }

        // 2) requireInitializer (only for VariableDeclarationExpr)
        if (Boolean.TRUE.equals(m.requireInitializer) && node instanceof VariableDeclarationExpr vde) {
            boolean allInit = vde.getVariables().stream().allMatch(v -> v.getInitializer().isPresent());
            if (!allInit) return false;
        }

        // 3) fqn (requires symbol solver) – now handles method calls too
        if (m.fqn != null) {
            String resolved = null;
            try {
                if (node instanceof MethodCallExpr mc) {
                    // resolve the method, then get its declaring type
                    var rmd = mc.resolve();
                    resolved = rmd.declaringType().getQualifiedName();
                } else if (node instanceof ObjectCreationExpr oce) {
                    resolved = JavaParserFacade
                                .get(typeSolver)
                                .getType(oce)
                                .describe();
                } else if (node instanceof VariableDeclarationExpr vde) {
                    resolved = JavaParserFacade
                                .get(typeSolver)
                                .getType(vde.getElementType())
                                .describe();
                } else if (node instanceof Parameter p) {
                    resolved = JavaParserFacade
                                .get(typeSolver)
                                .getType(p.getType())
                                .describe();
                }
            } catch (Exception ignored) {}
            if (!m.fqn.equals(resolved)) return false;
        }


        // 4) simple/resolved `type`
        if (m.type != null && node instanceof VariableDeclarationExpr vde2) {
            String simple = vde2.getElementType().asString();
            String resolved = null;
            try {
                resolved = vde2.getElementType().resolve().asReferenceType().getQualifiedName();
            } catch (Exception ignored) {}
            boolean ok = simple.equals(m.type)
                   || (resolved != null && resolved.equals(m.type))
                   || simple.endsWith("." + m.type);
            if (!ok) return false;
        }
        if (m.type != null && node instanceof Parameter p2) {
            String simple = p2.getType().asString();
            String resolved = null;
            try {
                resolved = p2.getType().resolve().asReferenceType().getQualifiedName();
            } catch (Exception ignored) {}
            boolean ok = simple.equals(m.type)
                   || (resolved != null && resolved.equals(m.type))
                   || simple.endsWith("." + m.type);
            if (!ok) return false;
        }

        // 5) methodName
        if (m.methodName != null) {
            if (!(node instanceof MethodCallExpr mc && mc.getNameAsString().equals(m.methodName)))
                return false;
        }

        // 6) fqnScope (for MethodCallExpr)
        if (m.fqnScope != null) {
            if (!(node instanceof MethodCallExpr mc && mc.getScope().isPresent()))
                return false;
            try {
                String sf = JavaParserFacade.get(typeSolver)
                              .getType(mc.getScope().get()).describe();
                if (!m.fqnScope.equals(sf)) return false;
            } catch (Exception ignore) { return false; }
        }

        // 7) annotation
        if (m.annotation != null) {
            if (node instanceof com.github.javaparser.ast.nodeTypes.NodeWithAnnotations<?> nwa) {
                if (!nwa.isAnnotationPresent(m.annotation)) return false;
            } else return false;
        }

        // 8) typePattern
        if (m.typePattern != null) {
            String text = node.toString();
            if (!Pattern.compile(m.typePattern).matcher(text).find()) return false;
        }

        // skip any VDE whose element type carries type arguments
        if (Boolean.TRUE.equals(m.requireNoTypeArgs) && node instanceof VariableDeclarationExpr vde) {
            var elem = vde.getElementType();
            if (elem.isClassOrInterfaceType()) {
                var cit = elem.asClassOrInterfaceType();
                if (cit.getTypeArguments().isPresent() && !cit.getTypeArguments().get().isEmpty()) {
                    return false;
                }
            }
        }

        // NEW: for BinaryExpr operator matching
        if (m.operator != null) {
            if (node instanceof com.github.javaparser.ast.expr.BinaryExpr be) {
                if (!be.getOperator().asString().equals(m.operator)) return false;
            } else return false;
        }

        if (m.literalOnly != null && node instanceof BinaryExpr be) {
            List<String> parts = new ArrayList<>();
            if (m.literalOnly && !ConcatUtils.gatherLiterals(be, parts)) {
                return false;
            }
        }
        if (m.literalPattern != null && node instanceof BinaryExpr be2) {
            List<String> parts = new ArrayList<>();
            if (!ConcatUtils.gatherLiterals(be2, parts)) {
                return false;
            }
            String joined = String.join("", parts);
            if (!Pattern.compile(m.literalPattern).matcher(joined).find()) {
                return false;
            }
        }



        // (note: argumentType/expectedParamType handled in a later validation pass)

        return true;
    }
}
