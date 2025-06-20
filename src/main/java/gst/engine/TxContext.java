package gst.engine;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;

import gst.engine.validator.ValidationError;

public class TxContext {
    private final Map<Path, CompilationUnit> originalFiles = new HashMap<>();
    private final Map<Node, Node> originalNodes = new HashMap<>();
    private final Map<String, Set<Node>> recipeChanges = new HashMap<>();
    private final Set<Path> changedFiles = new HashSet<>();
    private final Map<String, String> varTypeChanges = new HashMap<>();
    private final Set<Path> rolledBackFiles = new HashSet<>();
    private final Set<Path> successfullyTransformedFiles = new HashSet<>();
    private final Map<Path, Set<String>> fileToRecipes = new HashMap<>();
    private final Map<Path, List<ValidationError>> rollbackErrors = new HashMap<>();

    public void registerRecipeForFile(Path file, String recipeName) {
        fileToRecipes.computeIfAbsent(file, k -> new HashSet<>()).add(recipeName);
    }

    public Set<String> getRecipesForFile(Path file) {
        return fileToRecipes.getOrDefault(file, Set.of());
    }

    public void recordRollbackError(Path file, List<ValidationError> errors) {
        rollbackErrors.put(file, errors);
    }

    public List<ValidationError> getRollbackErrors(Path file) {
        return rollbackErrors.getOrDefault(file, List.of());
    }

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


    public void registerVarType(String varName, String newType) {
        varTypeChanges.put(varName, newType);
    }

    public Optional<String> getVarType(String varName) {
        return Optional.ofNullable(varTypeChanges.get(varName));
    }

    public boolean hasVarChanged(String varName) {
        return varTypeChanges.containsKey(varName);
    }



    public void saveOriginalFile(Path file, CompilationUnit cu) {
        originalFiles.putIfAbsent(file, cu.clone());
    }

    public Optional<CompilationUnit> getOriginalFile(Path file) {
        return Optional.ofNullable(originalFiles.get(file));
    }

    public void saveOriginalNode(Node modified, Node original) {
        originalNodes.put(modified, original.clone());
    }

    public Optional<Node> getOriginalNode(Node modified) {
        return Optional.ofNullable(originalNodes.get(modified));
    }

    public void registerRecipeChange(String recipeName, Node node) {
        recipeChanges.computeIfAbsent(recipeName, k -> new HashSet<>()).add(node);
    }

    public Set<Node> getRecipeChanges(String recipeName) {
        return recipeChanges.getOrDefault(recipeName, Set.of());
    }

    public void markFileChanged(Path file) {
        changedFiles.add(file);
    }

    public boolean isFileChanged(Path file) {
        return changedFiles.contains(file);
    }

    public Set<Path> getChangedFiles() {
        return changedFiles;
    }
}
