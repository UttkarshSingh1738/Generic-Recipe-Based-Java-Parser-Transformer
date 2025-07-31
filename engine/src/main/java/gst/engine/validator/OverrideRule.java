package gst.engine.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.nodeTypes.NodeWithAnnotations;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;

import gst.engine.TxContext;

/**
 * Validates that @Override annotations are correctly placed.
 */
public class OverrideRule implements ValidationRule {
    @Override
    public List<ValidationError> validateRecipeChanges(
        CompilationUnit cu,
        List<Node> changedNodes, 
        TxContext context,
        JavaSymbolSolver solver,
        String recipeName
    ) {
        List<ValidationError> errors = new ArrayList<>();
        String filePath = cu.getStorage()
                            .map(s -> s.getPath().toString())
                            .orElse("<unknown>");

        // Only validate method declarations in changed nodes
        for (Node node : changedNodes) {
            node.findAll(MethodDeclaration.class).forEach(md -> {
                if (!(md instanceof NodeWithAnnotations<?> nwa && nwa.isAnnotationPresent("Override")))
                    return;

                ResolvedMethodDeclaration rmd;
                try {
                    rmd = md.resolve();
                } catch (Exception e) {
                    return;
                }

                ResolvedReferenceTypeDeclaration declaringType;
                try {
                    declaringType = rmd.declaringType();
                } catch (Exception e) {
                    return;
                }
                
                boolean overrides = false;

                List<ResolvedReferenceType> ancestors;
                try {
                    ancestors = declaringType.getAllAncestors();
                } catch (Exception e) {
                    ancestors = List.of();
                }

                for (ResolvedReferenceType ancestorRef : ancestors) {
                    Optional<ResolvedReferenceTypeDeclaration> optDecl;
                    try {
                        optDecl = ancestorRef.getTypeDeclaration();
                    } catch (Exception e) {
                        continue;
                    }
                    if (optDecl.isEmpty()) continue;
                    ResolvedReferenceTypeDeclaration ancestorTD = optDecl.get();

                    try {
                        for (ResolvedMethodDeclaration ancMd : ancestorTD.getDeclaredMethods()) {
                            if (sameSignature(rmd, ancMd)) {
                                overrides = true;
                                break;
                            }
                        }
                    } catch (Exception e) {
                    }
                    if (overrides) break;
                }

                if (!overrides) {
                    String msg = String.format(
                        "Method '%s' is annotated @Override but does not override any superclass/interface method",
                        md.getNameAsString()
                    );
                    errors.add(new ValidationError(filePath, (Node)md, msg));
                }
            });
        }

        return errors;
    }
    
    @Override
    public String getRuleName() {
        return "OverrideRule";
    }

    private boolean sameSignature(
        ResolvedMethodDeclaration a,
        ResolvedMethodDeclaration b
    ) {
        if (!a.getName().equals(b.getName())) return false;
        if (a.getNumberOfParams() != b.getNumberOfParams()) return false;
        for (int i = 0; i < a.getNumberOfParams(); i++) {
            String tA = a.getParam(i).getType().describe();
            String tB = b.getParam(i).getType().describe();
            if (!tA.equals(tB)) return false;
        }
        return true;
    }
}