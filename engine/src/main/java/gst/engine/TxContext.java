package gst.engine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import gst.engine.validator.ValidationError;

/**
 * Transaction context for tracking recipe transformations and managing rollbacks.
 * Provides granular tracking of changes per recipe and file for targeted rollback operations.
 * 
 * <h2>Architecture Note: Node Tracking for Rollback</h2>
 * <p>The Pipeline automatically saves the matched node before applying each action via
 * {@link #saveOriginalNodeForRecipe(String, Node, Node)}. Actions should NOT call
 * {@link #saveOriginalNode(Node, Node)} as it is deprecated and non-functional.</p>
 * 
 * <p><strong>Known Limitation:</strong> Some actions (e.g., RenameVariableAction) modify
 * additional nodes beyond the matched node (e.g., all references to a variable). Currently,
 * these additional modifications are NOT tracked for rollback. This is a known architectural
 * limitation that should be addressed in a future refactoring.</p>
 * 
 * <p><strong>Future Enhancement:</strong> Consider implementing a generalized node tracking
 * mechanism that captures all AST modifications, not just the initially matched node.</p>
 */
public class TxContext {
    // File and transformation tracking
    private final Map<Path, CompilationUnit> originalFiles = new HashMap<>();
    private final Set<Path> rolledBackFiles = new HashSet<>();
    private final Set<Path> successfullyTransformedFiles = new HashSet<>();
    private final Map<Path, Set<String>> fileToRecipes = new HashMap<>();
    private final Map<Path, List<ValidationError>> rollbackErrors = new HashMap<>();
    
    // Recipe-specific change tracking using Lists to avoid node equality issues
    private final Map<String, List<Node>> recipeChanges = new HashMap<>();
    private final Map<String, Map<Node, Node>> recipeOriginalNodes = new HashMap<>();
    
    // Variable type tracking for compatibility validation
    private final Map<String, String> varTypeChanges = new HashMap<>();

    // Recipe and file association
    public void registerRecipeForFile(Path file, String recipeName) {
        fileToRecipes.computeIfAbsent(file, k -> new HashSet<>()).add(recipeName);
    }

    public Set<String> getRecipesForFile(Path file) {
        return fileToRecipes.getOrDefault(file, Set.of());
    }

    // Rollback error management  
    public void recordRollbackError(Path file, List<ValidationError> errors) {
        rollbackErrors.put(file, errors);
    }

    public List<ValidationError> getRollbackErrors(Path file) {
        return rollbackErrors.getOrDefault(file, List.of());
    }

    // File status tracking
    public void markRolledBack(Path file) {
        rolledBackFiles.add(file);
    }

    public void markTransformed(Path file) {
        successfullyTransformedFiles.add(file);
    }

    public Set<Path> getRolledBackFiles() {
        return rolledBackFiles;
    }

    public Set<Path> getTransformedFiles() {
        return successfullyTransformedFiles;
    }

    // Variable type tracking for compatibility validation
    // Check TypeCompatibilityRule and ChangeTypeAction for usage
    public void registerVarType(String varName, String newType) {
        varTypeChanges.put(varName, newType);
    }

    public Optional<String> getVarType(String varName) {
        return Optional.ofNullable(varTypeChanges.get(varName));
    }

    public boolean hasVarChanged(String varName) {
        return varTypeChanges.containsKey(varName);
    }

    // Original file preservation
    public void saveOriginalFile(Path file, CompilationUnit cu) {
        originalFiles.putIfAbsent(file, cu.clone());
    }

    public Optional<CompilationUnit> getOriginalFile(Path file) {
        return Optional.ofNullable(originalFiles.get(file));
    }

    // Recipe-specific node tracking for targeted rollbacks
    public void saveOriginalNodeForRecipe(String recipeName, Node modified, Node original) {
        recipeOriginalNodes.computeIfAbsent(recipeName, k -> new HashMap<>()).put(modified, original.clone());
    }

    /**
     * @deprecated This method is no longer used. Node tracking is handled automatically
     * by the Pipeline via {@link #saveOriginalNodeForRecipe(String, Node, Node)}.
     * Actions should NOT call this method.
     */
    @Deprecated
    public void saveOriginalNode(Node modified, Node original) {
        // LEGACY: Handled at Pipeline level for recipe-specific tracking
        // Actions do not need to call this - Pipeline handles it automatically
    }

    public void registerRecipeChange(String recipeName, Node node) {
        recipeChanges.computeIfAbsent(recipeName, k -> new ArrayList<>()).add(node);
    }

    public List<Node> getRecipeChanges(String recipeName) {
        return recipeChanges.getOrDefault(recipeName, List.of());
    }

    public Map<Node, Node> getRecipeOriginalNodes(String recipeName) {
        return recipeOriginalNodes.getOrDefault(recipeName, Map.of());
    }

    // Targeted recipe rollback
    public void rollbackRecipe(String recipeName) {
        Map<Node, Node> originals = getRecipeOriginalNodes(recipeName);
        List<Node> changedNodes = getRecipeChanges(recipeName);
        
        for (Node changedNode : changedNodes) {
            Node original = originals.get(changedNode);
            if (original != null) {
                changedNode.replace(original.clone());
            }
        }
        
        recipeChanges.remove(recipeName);
        recipeOriginalNodes.remove(recipeName);
    }
}
