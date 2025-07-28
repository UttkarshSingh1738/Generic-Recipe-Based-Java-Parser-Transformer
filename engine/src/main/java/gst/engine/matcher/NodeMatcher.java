package gst.engine.matcher;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
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
            case "CatchClause" ->
                root.findAll(CatchClause.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "FieldDeclaration" ->
                root.findAll(FieldDeclaration.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "ConstructorDeclaration" ->
                root.findAll(ConstructorDeclaration.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "MethodDeclaration" ->
                root.findAll(MethodDeclaration.class).stream().map(n -> (Node) n).collect(Collectors.toList());
            case "BlockStmt" ->
                root.findAll(com.github.javaparser.ast.stmt.BlockStmt.class).stream()
                        .map(n -> (Node) n).collect(Collectors.toList());
            case "VariableDeclarator" ->
                root.findAll(com.github.javaparser.ast.body.VariableDeclarator.class).stream()
                        .map(n -> (Node) n).collect(Collectors.toList());
            default -> {
                System.err.println("[CANDIDATE-ERROR] Unknown nodeType: '" + nodeType + "'");
                yield List.of();
            }
        };
    }

    public static MatchResult matches(
            Node node,
            Match m,
            CombinedTypeSolver typeSolver
    ) {
        List<String> failures = new ArrayList<>();

        // nodeType sanity
        String actualType = node.getClass().getSimpleName();
        if (!actualType.equals(m.nodeType)) {
            failures.add("nodeType mismatch: expected `" + m.nodeType +
                         "` but got `" + actualType + "`");
        }

        // matchExpr: regex against the node’s source text
        if (m.matchExpr != null) {
            try {
                if (!Pattern.compile(m.matchExpr).matcher(node.toString()).find()) {
                    failures.add("matchExpr failed: `" + m.matchExpr + "`");
                }
            } catch (Exception e) {
                failures.add("invalid matchExpr regex `" + m.matchExpr
                             + "`: " + e.getMessage());
            }
        }

        // requiresImport / forbidsImport
        if (m.requiresImport != null || m.forbidsImport != null) {
            Optional<CompilationUnit> cuOpt = node.findCompilationUnit();
            if (cuOpt.isEmpty()) {
                failures.add("no CompilationUnit to check imports");
            } else {
                var cu = cuOpt.get();
                if (m.requiresImport != null &&
                    cu.getImports().stream()
                      .noneMatch(i -> i.getNameAsString().equals(m.requiresImport))) {
                    failures.add("requiresImport not found: " + m.requiresImport);
                }
                if (m.forbidsImport != null &&
                    cu.getImports().stream()
                      .anyMatch(i -> i.getNameAsString().equals(m.forbidsImport))) {
                    failures.add("forbidsImport present: " + m.forbidsImport);
                }
            }
        }

        // requireInitializer (only for VariableDeclarationExpr)
        if (Boolean.TRUE.equals(m.requireInitializer) && node instanceof VariableDeclarationExpr vde) {
            boolean allInit = vde.getVariables().stream().allMatch(v -> v.getInitializer().isPresent());
            if (!allInit) {
                failures.add("requireInitializer failed: not all variables have initializers");
            }
        }

        // fqn (requires symbol solver) – now handles method calls too
        if (m.fqn != null) {
            String resolved = null;
            try {
                if (node instanceof MethodCallExpr mc) {
                    var rmd = mc.resolve();
                    resolved = rmd.declaringType().getQualifiedName();
                } else if (node instanceof ObjectCreationExpr oce) {
                    resolved = JavaParserFacade
                            .get(typeSolver)
                            .getType(oce)
                            .describe();
                } else if (node instanceof VariableDeclarationExpr vde2) {
                    resolved = JavaParserFacade
                            .get(typeSolver)
                            .getType(vde2.getElementType())
                            .describe();
                } else if (node instanceof Parameter p) {
                    resolved = JavaParserFacade
                            .get(typeSolver)
                            .getType(p.getType())
                            .describe();
                }
            } catch (Exception e) {
                failures.add("fqn resolution error: " + e.getMessage());
            }
            if (!m.fqn.equals(resolved)) {
                failures.add("fqn mismatch: expected `" + m.fqn + "` but got `" + resolved + "`");
            }
        }

        // typeAny for variables/parameters
        if (m.typeAny != null && node instanceof VariableDeclarationExpr vdeAny) {
            String simple = vdeAny.getElementType().asString();
            final String[] resolvedHolder = new String[1];
            try {
                resolvedHolder[0] = vdeAny.getElementType().resolve().asReferenceType().getQualifiedName();
            } catch (Exception e) {
                failures.add("typeAny resolution error: " + e.getMessage());
            }
            boolean ok = m.typeAny.stream().anyMatch(t -> t.equals(simple) || t.equals(resolvedHolder[0]) || simple.endsWith("." + t));
            if (!ok) {
                failures.add("typeAny mismatch: got `" + simple + "` and `" + resolvedHolder[0] + "`");
            }
        }
        if (m.typeAny != null && node instanceof Parameter pAny) {
            String simple = pAny.getType().asString();
            final String[] resolvedHolder = new String[1];
            try {
                resolvedHolder[0] = pAny.getType().resolve().asReferenceType().getQualifiedName();
            } catch (Exception e) {
                failures.add("typeAny resolution error: " + e.getMessage());
            }
            boolean ok = m.typeAny.stream().anyMatch(t -> t.equals(simple) || t.equals(resolvedHolder[0]) || simple.endsWith("." + t));
            if (!ok) {
                failures.add("typeAny mismatch: got `" + simple + "` and `" + resolvedHolder[0] + "`");
            }
        }

        // simple/resolved `type`
        if (m.type != null && node instanceof VariableDeclarationExpr vde2) {
            String simple = vde2.getElementType().asString();
            String resolved = null;
            try {
                resolved = vde2.getElementType().resolve().asReferenceType().getQualifiedName();
            } catch (Exception e) {
                failures.add("type resolution error: " + e.getMessage());
            }
            boolean ok = simple.equals(m.type)
                    || (resolved != null && resolved.equals(m.type))
                    || simple.endsWith("." + m.type);
            if (!ok) {
                failures.add("type mismatch: got `" + simple + "` and `" + resolved + "`");
            }
        }
        if (m.type != null && node instanceof Parameter p2) {
            String simple = p2.getType().asString();
            String resolved = null;
            try {
                resolved = p2.getType().resolve().asReferenceType().getQualifiedName();
            } catch (Exception e) {
                failures.add("type resolution error: " + e.getMessage());
            }
            boolean ok = simple.equals(m.type)
                    || (resolved != null && resolved.equals(m.type))
                    || simple.endsWith("." + m.type);
            if (!ok) {
                failures.add("type mismatch: got `" + simple + "` and `" + resolved + "`");
            }
        }

        // methodName (for calls AND declarations)
        if (m.methodName != null) {
            if (node instanceof MethodCallExpr mc) {
                if (!mc.getNameAsString().equals(m.methodName)) {
                    failures.add("methodName mismatch: expected `" + m.methodName + "` but got `" + mc.getNameAsString() + "`");
                }
            } else if (node instanceof MethodDeclaration md) {
                if (!md.getNameAsString().equals(m.methodName)) {
                    failures.add("methodName mismatch: expected `" + m.methodName + "` but got `" + md.getNameAsString() + "`");
                }
            } else {
                failures.add("methodName not applicable to node type: " + node.getClass().getSimpleName());
            }
        }

        // argumentType + expectedParamType for MethodCallExpr
        if (m.argumentType != null && m.expectedParamType != null && node instanceof MethodCallExpr mcArg) {
            ResolvedMethodDeclaration rmd = null;
            try {
                rmd = mcArg.resolve();
            } catch (Exception e) {
                failures.add("argumentType/expectedParamType: could not resolve method: " + e.getMessage());
            }
            boolean matched = false;
            if (rmd != null) {
                for (int i = 0; i < mcArg.getArguments().size() && i < rmd.getNumberOfParams(); i++) {
                    String argT, paramT;
                    try {
                        argT   = mcArg.getArgument(i).calculateResolvedType().describe();
                        paramT = rmd.getParam(i).getType().describe();
                    } catch (Exception e) {
                        continue;
                    }
                    if (argT.equals(m.argumentType) && paramT.equals(m.expectedParamType)) {
                        matched = true;
                        break;
                    }
                }
            }
            if (!matched) {
                failures.add("argumentType/expectedParamType mismatch: did not find argument of type `" + m.argumentType + "` and parameter of type `" + m.expectedParamType + "`");
            }
        }

        // fqnScope (for MethodCallExpr)
        if (m.fqnScope != null) {
            if (!(node instanceof MethodCallExpr mc && mc.getScope().isPresent())) {
                failures.add("fqnScope: not a MethodCallExpr with scope");
            } else {
                try {
                    String sf = JavaParserFacade.get(typeSolver)
                            .getType(mc.getScope().get()).describe();
                    if (!m.fqnScope.equals(sf)) {
                        failures.add("fqnScope mismatch: expected `" + m.fqnScope + "` but got `" + sf + "`");
                    }
                } catch (Exception e) {
                    failures.add("fqnScope resolution error: " + e.getMessage());
                }
            }
        }

        // annotation
        if (m.annotation != null) {
            if (node instanceof com.github.javaparser.ast.nodeTypes.NodeWithAnnotations<?> nwa) {
                if (!nwa.isAnnotationPresent(m.annotation)) {
                    failures.add("annotation not present: " + m.annotation);
                }
            } else {
                failures.add("annotation: node is not NodeWithAnnotations");
            }
        }

        // annotationValuePattern
        if (m.annotationValuePattern != null) {
            boolean ok = false;
            if (node instanceof com.github.javaparser.ast.nodeTypes.NodeWithAnnotations<?> nwa) {
                for (AnnotationExpr ann : nwa.getAnnotations()) {
                    String txt = ann.toString();
                    if (Pattern.compile(m.annotationValuePattern).matcher(txt).find()) {
                        ok = true;
                        break;
                    }
                }
            }
            if (!ok) failures.add("annotationValuePattern not matched: " + m.annotationValuePattern);
        }

        // typePattern
        if (m.typePattern != null) {
            String text = node.toString();
            if (!Pattern.compile(m.typePattern).matcher(text).find()) {
                failures.add("typePattern not matched: " + m.typePattern);
            }
        }

        // requireNoTypeArgs
        if (Boolean.TRUE.equals(m.requireNoTypeArgs) && node instanceof VariableDeclarationExpr vde3) {
            var elem = vde3.getElementType();
            if (elem.isClassOrInterfaceType()) {
                var cit = elem.asClassOrInterfaceType();
                if (cit.getTypeArguments().isPresent() && !cit.getTypeArguments().get().isEmpty()) {
                    failures.add("requireNoTypeArgs failed: type has generic arguments");
                }
            }
        }

        // operator (for BinaryExpr)
        if (m.operator != null) {
            if (node instanceof com.github.javaparser.ast.expr.BinaryExpr be) {
                if (!be.getOperator().asString().equals(m.operator)) {
                    failures.add("operator mismatch: expected `" + m.operator + "` but got `" + be.getOperator().asString() + "`");
                }
            } else {
                failures.add("operator: node is not BinaryExpr");
            }
        }

        // literalOnly / literalPattern (for BinaryExpr)
        if (node instanceof BinaryExpr beLit) {
            if (Boolean.TRUE.equals(m.literalOnly)) {
                List<String> parts = new ArrayList<>();
                if (!ConcatUtils.gatherLiterals(beLit, parts)) {
                    failures.add("literalOnly failed: not all operands are literals");
                }
            }
            if (m.literalPattern != null) {
                List<String> parts = new ArrayList<>();
                if (!ConcatUtils.gatherLiterals(beLit, parts)) {
                    failures.add("literalPattern failed: not all operands are literals");
                } else {
                    String joined = String.join("", parts);
                    if (!Pattern.compile(m.literalPattern).matcher(joined).find()) {
                        failures.add("literalPattern not matched: " + m.literalPattern);
                    }
                }
            }
        }

        // ForStmt loop patterns
        if (node instanceof ForStmt fs) {
            // initVarPattern
            if (m.initVarPattern != null) {
                var inits = fs.getInitialization();
                if (inits.size() != 1 || !(inits.get(0) instanceof VariableDeclarationExpr vde4)
                        || vde4.getVariables().size() != 1) {
                    failures.add("initVarPattern failed: could not find single loop variable");
                } else {
                    String varName = vde4.getVariables().get(0).getNameAsString();
                    if (!Pattern.compile(m.initVarPattern).matcher(varName).find()) {
                        failures.add("initVarPattern not matched: " + m.initVarPattern);
                    }
                }
            }

            // conditionPattern
            if (m.conditionPattern != null) {
                var cmp = fs.getCompare().orElse(null);
                if (cmp == null || !Pattern.compile(m.conditionPattern).matcher(cmp.toString()).find()) {
                    failures.add("conditionPattern not matched: " + m.conditionPattern);
                }
            }

            // updatePattern
            if (m.updatePattern != null) {
                boolean anyMatch = fs.getUpdate().stream()
                        .anyMatch(u -> Pattern.compile(m.updatePattern).matcher(u.toString()).find());
                if (!anyMatch) {
                    failures.add("updatePattern not matched: " + m.updatePattern);
                }
            }

            // accessPattern (scan the whole body text)
            if (m.accessPattern != null) {
                String bodyText = fs.getBody().toString();
                if (!Pattern.compile(m.accessPattern).matcher(bodyText).find()) {
                    failures.add("accessPattern not matched: " + m.accessPattern);
                }
            }
        }

        // parentNodeType
        if (m.parentNodeType != null) {
            if (!node.getParentNode().map(p -> p.getClass().getSimpleName().equals(m.parentNodeType)).orElse(false)) {
                failures.add("parentNodeType mismatch: expected `" + m.parentNodeType + "`");
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
                failures.add("namePattern not matched: " + m.namePattern);
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
                failures.add("scopePattern not matched: " + m.scopePattern);
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
                failures.add("hasModifier not present: " + m.hasModifier);
            }
        }

        // returnTypePattern (for MethodDeclaration)
        if (m.returnTypePattern != null && node instanceof com.github.javaparser.ast.body.MethodDeclaration md2) {
            String rt = md2.getType().toString();
            if (!Pattern.compile(m.returnTypePattern).matcher(rt).find()) {
                failures.add("returnTypePattern not matched: " + m.returnTypePattern);
            }
        }

        // paramCount (for MethodDeclaration)
        if (m.paramCount != null && node instanceof com.github.javaparser.ast.body.MethodDeclaration md3) {
            if (md3.getParameters().size() != m.paramCount) {
                failures.add("paramCount mismatch: expected " + m.paramCount + " but got " + md3.getParameters().size());
            }
        }

        // beforeLine / afterLine (if implemented)
        if (m.beforeLine != null || m.afterLine != null) {
            Optional<Range> r = node.getRange();
            if (r.isEmpty()) {
                failures.add("no Range info for beforeLine/afterLine");
            } else {
                int line = r.get().begin.line;
                if (m.beforeLine != null && line >= m.beforeLine) {
                    failures.add("beforeLine failed: node starts at line " + line + ", expected before " + m.beforeLine);
                }
                if (m.afterLine != null && line <= m.afterLine) {
                    failures.add("afterLine failed: node starts at line " + line + ", expected after " + m.afterLine);
                }
            }
        }

        // overridesFqn: only MethodDeclarations overriding interface.method
        if (m.overridesFqn != null && node instanceof MethodDeclaration md4) {
            ResolvedMethodDeclaration rmd = null;
            try { rmd = md4.resolve(); } catch (Exception e) { failures.add("overridesFqn: could not resolve method: " + e.getMessage()); }
            boolean ok = false;
            if (rmd != null) {
                ok = rmd.declaringType().getAllAncestors().stream()
                    .map(anc -> {
                        try { return anc.getTypeDeclaration().orElse(null); }
                        catch (Exception e) { return null; }
                    })
                    .filter(Objects::nonNull)
                    .anyMatch(td -> td.getQualifiedName().equals(m.overridesFqn));
            }
            if (!ok) failures.add("overridesFqn not matched: " + m.overridesFqn);
        }

        // declaringFqn: only MethodCallExprs whose target belongs to interface
        if (m.declaringFqn != null && node instanceof MethodCallExpr mc2) {
            ResolvedMethodDeclaration rmd = null;
            try { rmd = mc2.resolve(); } catch(Exception e) { failures.add("declaringFqn: could not resolve method: " + e.getMessage()); }
            String declType = rmd != null ? rmd.declaringType().getQualifiedName() : null;
            if (declType == null || !declType.equals(m.declaringFqn)) failures.add("declaringFqn not matched: expected `" + m.declaringFqn + "` but got `" + declType + "`");
        }

        // declaringFqnPattern: match calls OR declarations whose declaring‐type name fits the regex
        if (m.declaringFqnPattern != null) {
            Pattern pat = Pattern.compile(m.declaringFqnPattern);
            if (node instanceof MethodCallExpr mc3) {
                ResolvedMethodDeclaration rmd = null;
                try { rmd = mc3.resolve(); } catch (Exception e) { failures.add("declaringFqnPattern: could not resolve method: " + e.getMessage()); }
                String declType = rmd != null ? rmd.declaringType().getQualifiedName() : null;
                if (declType == null || !pat.matcher(declType).matches()) {
                    failures.add("declaringFqnPattern not matched: " + m.declaringFqnPattern);
                }
            } else if (node instanceof MethodDeclaration md5) {
                ResolvedMethodDeclaration rmd = null;
                try { rmd = md5.resolve(); } catch (Exception e) { failures.add("declaringFqnPattern: could not resolve method: " + e.getMessage()); }
                String declType = rmd != null ? rmd.declaringType().getQualifiedName() : null;
                if (declType == null || !pat.matcher(declType).matches()) {
                    failures.add("declaringFqnPattern not matched: " + m.declaringFqnPattern);
                }
            } else {
                failures.add("declaringFqnPattern: not applicable to node type: " + node.getClass().getSimpleName());
            }
        }

        // overridesFqnPattern: match only MethodDeclarations overriding an interface/class whose FQN fits the regex
        if (m.overridesFqnPattern != null && node instanceof MethodDeclaration md6) {
            Pattern pat = Pattern.compile(m.overridesFqnPattern);
            ResolvedMethodDeclaration rmd = null;
            try { rmd = md6.resolve(); } catch (Exception e) { failures.add("overridesFqnPattern: could not resolve method: " + e.getMessage()); }
            boolean found = false;
            if (rmd != null) {
                found = rmd.declaringType()
                    .getAllAncestors().stream()
                    .flatMap( ancestorRef -> {
                        try {
                            return ancestorRef.getTypeDeclaration().stream();
                        } catch (Exception e) {
                            return Stream.<ResolvedReferenceTypeDeclaration>empty();
                        }
                    })
                    .map(ResolvedReferenceTypeDeclaration::getQualifiedName)
                    .anyMatch(pat.asPredicate());
            }
            if (!found) {
                failures.add("overridesFqnPattern not matched: " + m.overridesFqnPattern);
            }
        }

        if (failures.isEmpty()) {
            return MatchResult.success();
        } else {
            return new MatchResult(false, failures);
        }
    }
}
