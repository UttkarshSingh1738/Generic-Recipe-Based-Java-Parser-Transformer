package gst.engine.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;

import gst.api.Match;
import gst.engine.utils.ConcatUtils;

public class NodeMatcher {

    public static List<Node> findCandidates(Node root, String nodeType) {
        return switch (nodeType) {
            case "ObjectCreationExpr" ->
                root.findAll(ObjectCreationExpr.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "VariableDeclarationExpr" ->
                root.findAll(VariableDeclarationExpr.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "MethodCallExpr" ->
                root.findAll(MethodCallExpr.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "FieldAccessExpr" ->
                root.findAll(FieldAccessExpr.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "AnnotationExpr" ->
                root.findAll(AnnotationExpr.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "ImportDeclaration" ->
                root.findAll(ImportDeclaration.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "NameExpr" ->
                root.findAll(NameExpr.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "Parameter" ->
                root.findAll(Parameter.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "ClassOrInterfaceType" ->
                root.findAll(ClassOrInterfaceType.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "SwitchStmt" ->
                root.findAll(SwitchStmt.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "BinaryExpr" ->
                root.findAll(BinaryExpr.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "ForStmt" ->
                root.findAll(ForStmt.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "ExpressionStmt" ->
                root.findAll(ExpressionStmt.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "ClassOrInterfaceDeclaration" ->
                root.findAll(ClassOrInterfaceDeclaration.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            default ->
                List.of();
        };
    }

    public static boolean matches(
            Node node,
            Match m,
            CombinedTypeSolver typeSolver
    ) {
        // nodeType sanity
        if (!node.getClass().getSimpleName().equals(m.nodeType)) {
            return false;
        }

        // matchExpr: regex against the node’s source text
        if (m.matchExpr != null) {
            String text = node.toString();
            if (!Pattern.compile(m.matchExpr).matcher(text).find()) {
                return false;
            }
        }

        // requiresImport / forbidsImport
        if (m.requiresImport != null || m.forbidsImport != null) {
            Optional<CompilationUnit> cuOpt = node.findCompilationUnit();
            if (cuOpt.isEmpty()) {
                return false;
            }
            CompilationUnit cu = cuOpt.get();
            if (m.requiresImport != null) {
                boolean found = cu.getImports().stream()
                        .anyMatch(i -> i.getNameAsString().equals(m.requiresImport));
                if (!found) {
                    return false;
                }
            }
            if (m.forbidsImport != null) {
                boolean found = cu.getImports().stream()
                        .anyMatch(i -> i.getNameAsString().equals(m.forbidsImport));
                if (found) {
                    return false;
                }
            }
        }

        // requireInitializer (only for VariableDeclarationExpr)
        if (Boolean.TRUE.equals(m.requireInitializer) && node instanceof VariableDeclarationExpr vde) {
            boolean allInit = vde.getVariables().stream().allMatch(v -> v.getInitializer().isPresent());
            if (!allInit) {
                return false;
            }
        }

        // fqn (requires symbol solver) – now handles method calls too
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
            } catch (Exception ignored) {
            }
            if (!m.fqn.equals(resolved)) {
                return false;
            }
        }
        
        // typeAny for variables/parameters
        if (m.typeAny != null && node instanceof VariableDeclarationExpr vdeAny) {
            String simple = vdeAny.getElementType().asString();
            final String[] resolvedHolder = new String[1];
            try { resolvedHolder[0] = vdeAny.getElementType().resolve().asReferenceType().getQualifiedName(); }
            catch (Exception ignored) {}
            boolean ok = m.typeAny.stream().anyMatch(t->t.equals(simple) || t.equals(resolvedHolder[0]) || simple.endsWith("."+t));
            if (!ok) return false;
        }
        if (m.typeAny != null && node instanceof Parameter pAny) {
            String simple = pAny.getType().asString();
            final String[] resolvedHolder = new String[1];
            try { resolvedHolder[0] = pAny.getType().resolve().asReferenceType().getQualifiedName(); }
            catch (Exception ignored) {}
            boolean ok = m.typeAny.stream().anyMatch(t->t.equals(simple) || t.equals(resolvedHolder[0]) || simple.endsWith("."+t));
            if (!ok) return false;
        }

        // simple/resolved `type`
        if (m.type != null && node instanceof VariableDeclarationExpr vde2) {
            String simple = vde2.getElementType().asString();
            String resolved = null;
            try {
                resolved = vde2.getElementType().resolve().asReferenceType().getQualifiedName();
            } catch (Exception ignored) {
            }
            boolean ok = simple.equals(m.type)
                    || (resolved != null && resolved.equals(m.type))
                    || simple.endsWith("." + m.type);
            if (!ok) {
                return false;
            }
        }
        if (m.type != null && node instanceof Parameter p2) {
            String simple = p2.getType().asString();
            String resolved = null;
            try {
                resolved = p2.getType().resolve().asReferenceType().getQualifiedName();
            } catch (Exception ignored) {
            }
            boolean ok = simple.equals(m.type)
                    || (resolved != null && resolved.equals(m.type))
                    || simple.endsWith("." + m.type);
            if (!ok) {
                return false;
            }
        }

        // methodName
        if (m.methodName != null) {
            if (!(node instanceof MethodCallExpr mc && mc.getNameAsString().equals(m.methodName))) {
                return false;
            }
        }

        // argumentType + expectedParamType for MethodCallExpr
        // if (m.argumentType != null && m.expectedParamType != null && node instanceof MethodCallExpr mcArg) {
        //     var rmd = mcArg.resolve();
        //     boolean matched = false;
        //     for (int i = 0; i < mcArg.getArguments().size() && i < rmd.getNumberOfParams(); i++) {
        //         String argT = mcArg.getArgument(i).calculateResolvedType().describe();
        //         String paramT = rmd.getParam(i).getType().describe();
        //         if (argT.equals(m.argumentType) && paramT.equals(m.expectedParamType)) {
        //             matched = true; break;
        //         }
        //     }
        //     if (!matched) return false;
        // }

        // fqnScope (for MethodCallExpr)
        if (m.fqnScope != null) {
            if (!(node instanceof MethodCallExpr mc && mc.getScope().isPresent())) {
                return false;
            }
            try {
                String sf = JavaParserFacade.get(typeSolver)
                        .getType(mc.getScope().get()).describe();
                if (!m.fqnScope.equals(sf)) {
                    return false;
                }
            } catch (Exception ignore) {
                return false;
            }
        }

        // annotation
        if (m.annotation != null) {
            if (node instanceof com.github.javaparser.ast.nodeTypes.NodeWithAnnotations<?> nwa) {
                if (!nwa.isAnnotationPresent(m.annotation)) {
                    return false;
                }
            } else {
                return false;
            }
        }

        // typePattern
        if (m.typePattern != null) {
            String text = node.toString();
            if (!Pattern.compile(m.typePattern).matcher(text).find()) {
                return false;
            }
        }

        // requireNoTypeArgs
        if (Boolean.TRUE.equals(m.requireNoTypeArgs) && node instanceof VariableDeclarationExpr vde) {
            var elem = vde.getElementType();
            if (elem.isClassOrInterfaceType()) {
                var cit = elem.asClassOrInterfaceType();
                if (cit.getTypeArguments().isPresent() && !cit.getTypeArguments().get().isEmpty()) {
                    return false;
                }
            }
        }

        // operator (for BinaryExpr)
        if (m.operator != null) {
            if (node instanceof com.github.javaparser.ast.expr.BinaryExpr be) {
                if (!be.getOperator().asString().equals(m.operator)) {
                    return false;
                }
            } else {
                return false;
            }
        }

        // literalOnly / literalPattern (for BinaryExpr)
        if (node instanceof BinaryExpr beLit) {
            if (Boolean.TRUE.equals(m.literalOnly)) {
                List<String> parts = new ArrayList<>();
                if (!ConcatUtils.gatherLiterals(beLit, parts)) return false;
            }
            if (m.literalPattern != null) {
                List<String> parts = new ArrayList<>();
                if (!ConcatUtils.gatherLiterals(beLit, parts)) return false;
                String joined = String.join("", parts);
                if (!Pattern.compile(m.literalPattern).matcher(joined).find()) return false;
            }
        }

        // ForStmt loop patterns
        if (node instanceof ForStmt fs) {
            // initVarPattern
            if (m.initVarPattern != null) {
                var inits = fs.getInitialization();
                if (inits.size() != 1 || !(inits.get(0) instanceof VariableDeclarationExpr vde)
                        || vde.getVariables().size() != 1) {
                    return false;
                }
                String varName = vde.getVariables().get(0).getNameAsString();
                if (!Pattern.compile(m.initVarPattern).matcher(varName).find()) {
                    return false;
                }
            }

            // conditionPattern
            if (m.conditionPattern != null) {
                var cmp = fs.getCompare().orElse(null);
                if (cmp == null || !Pattern.compile(m.conditionPattern).matcher(cmp.toString()).find()) {
                    return false;
                }
            }

            // updatePattern
            if (m.updatePattern != null) {
                boolean anyMatch = fs.getUpdate().stream()
                        .anyMatch(u -> Pattern.compile(m.updatePattern).matcher(u.toString()).find());
                if (!anyMatch) {
                    return false;
                }
            }

            // accessPattern (scan the whole body text)
            if (m.accessPattern != null) {
                String bodyText = fs.getBody().toString();
                if (!Pattern.compile(m.accessPattern).matcher(bodyText).find()) {
                    return false;
                }
            }
        }

        // parentNodeType
        if (m.parentNodeType != null) {
            if (!node.getParentNode().map(p -> p.getClass().getSimpleName().equals(m.parentNodeType)).orElse(false)) {
                return false;
            }
        }

        // namePattern (for declarations and names)
        if (m.namePattern != null) {
            String name = null;
            if (node instanceof com.github.javaparser.ast.body.VariableDeclarator vd) {
                name = vd.getNameAsString();
            } else if (node instanceof com.github.javaparser.ast.body.MethodDeclaration md) {
                name = md.getNameAsString();
            } else if (node instanceof com.github.javaparser.ast.expr.NameExpr ne) {
                name = ne.getNameAsString();
            } else if (node instanceof ClassOrInterfaceDeclaration cd) {
                name = cd.getNameAsString();
            }
            if (name == null || !Pattern.compile(m.namePattern).matcher(name).find()) {
                return false;
            }
        }

        // scopePattern (for MethodCallExpr / FieldAccessExpr)
        if (m.scopePattern != null) {
            String scope = null;
            if (node instanceof com.github.javaparser.ast.expr.MethodCallExpr mc && mc.getScope().isPresent()) {
                scope = mc.getScope().get().toString();
            } else if (node instanceof com.github.javaparser.ast.expr.FieldAccessExpr fa) {
                scope = fa.getScope().toString();
            }
            if (scope == null || !Pattern.compile(m.scopePattern).matcher(scope).find()) {
                return false;
            }
        }

        // hasModifier (for nodes with modifiers)
        if (m.hasModifier != null) {
            boolean has = false;
            if (node instanceof com.github.javaparser.ast.nodeTypes.NodeWithModifiers<?> nw) {
                has = nw.getModifiers().stream()
                        .anyMatch(mod -> mod.getKeyword().asString().equalsIgnoreCase(m.hasModifier));
            }
            if (!has) {
                return false;
            }
        }

        // returnTypePattern (for MethodDeclaration)
        if (m.returnTypePattern != null && node instanceof com.github.javaparser.ast.body.MethodDeclaration md) {
            String rt = md.getType().toString();
            if (!Pattern.compile(m.returnTypePattern).matcher(rt).find()) {
                return false;
            }
        }

        // paramCount (for MethodDeclaration)
        if (m.paramCount != null && node instanceof com.github.javaparser.ast.body.MethodDeclaration md) {
            if (md.getParameters().size() != m.paramCount) {
                return false;
            }
        }

        // 24) beforeLine / afterLine (if implemented)
        if (m.beforeLine != null || m.afterLine != null) {
            Optional<Range> r = node.getRange();
            if (r.isEmpty()) return false;
            int line = r.get().begin.line;
            if (m.beforeLine != null && line >= m.beforeLine) return false;
            if (m.afterLine != null && line <= m.afterLine) return false;
        }
        
        return true;
    }
}
