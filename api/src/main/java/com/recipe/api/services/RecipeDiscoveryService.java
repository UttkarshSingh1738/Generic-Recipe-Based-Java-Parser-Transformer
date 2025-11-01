package com.recipe.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import gst.api.MappingLoader;
import gst.api.Recipe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class RecipeDiscoveryService {
    
    private static final Logger logger = LoggerFactory.getLogger(RecipeDiscoveryService.class);
    
    private final Path resourcesPath;
    private final Map<String, RecipeInfo> recipeCache = new HashMap<>();
    
    public RecipeDiscoveryService(@Value("${recipes.resources-path:./resources}") String resourcesPath) {
        this.resourcesPath = Paths.get(resourcesPath).toAbsolutePath().normalize();
        loadRecipesFromResources();
    }
    
    /**
     * Discover and load all recipe files from the resources directory
     */
    private void loadRecipesFromResources() {
        if (!Files.exists(resourcesPath) || !Files.isDirectory(resourcesPath)) {
            logger.warn("Resources path does not exist or is not a directory: {}", resourcesPath);
            return;
        }
        
        try (Stream<Path> paths = Files.walk(resourcesPath)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".json"))
                 .forEach(this::loadRecipeFile);
        } catch (IOException e) {
            logger.error("Failed to scan resources directory", e);
        }
        
        logger.info("Loaded {} recipes from resources directory", recipeCache.size());
    }
    
    private void loadRecipeFile(Path recipeFile) {
        try {
            String fileName = recipeFile.getFileName().toString();
            String recipeName = fileName.substring(0, fileName.length() - 5); // Remove .json
            
            // Skip old-recipes and other non-standard directories
            String relativePath = resourcesPath.relativize(recipeFile).toString().replace('\\', '/');
            Path parent = recipeFile.getParent();
            if (parent != null && parent.getFileName() != null && 
                parent.getFileName().toString().equals("old-recipes")) {
                return; // Skip files in old-recipes directory
            }
            if (relativePath.contains("old-recipes") || 
                relativePath.contains("old-recipes/") ||
                relativePath.startsWith("input/") ||
                fileName.equals("config.json") ||
                fileName.equals("mappingsV3.json") ||
                fileName.equals("sample-app-mappings.json") ||
                fileName.equals("test-mappings.json") ||
                fileName.equals("11-17-mappings.json") ||
                fileName.equals("11-17-v2-mappings.json") ||
                fileName.equals("17-specific-mappings.json") ||
                fileName.equals("17-specific-v2-mappings.json") ||
                fileName.equals("method-target-to-static-test.json")) {
                return;
            }
            
            // Try to load as recipe file
            List<Recipe> recipes = MappingLoader.load(recipeFile);
            
            if (!recipes.isEmpty()) {
                Recipe firstRecipe = recipes.get(0);
                
                // Use recipe's description if available, otherwise generate from name
                String description = firstRecipe.description != null && !firstRecipe.description.trim().isEmpty()
                    ? firstRecipe.description
                    : extractDescriptionFromName(firstRecipe.name != null ? firstRecipe.name : recipeName);
                
                // Display name: use recipe's internal name if it's descriptive, otherwise use filename
                String displayName = firstRecipe.name != null && firstRecipe.name.length() > 0
                    ? firstRecipe.name
                    : recipeName;
                
                RecipeInfo info = new RecipeInfo(
                    recipeName, // This is the key identifier (filename without .json)
                    displayName, // Display name shown to user
                    description, // Description from recipe or generated
                    recipeFile.toString(),
                    recipes
                );
                recipeCache.put(recipeName, info);
                logger.debug("Loaded recipe: {} (display: {}) from {}", recipeName, displayName, recipeFile);
            }
        } catch (Exception e) {
            logger.warn("Failed to load recipe file: " + recipeFile + " - " + e.getMessage(), e);
        }
    }
    
    private String extractDescriptionFromName(String name) {
        if (name == null || name.isEmpty()) {
            return "Recipe transformation";
        }
        
        // Convert camelCase, kebab-case, or snake_case to readable description
        String readable = name
            .replaceAll("([a-z])([A-Z])", "$1 $2") // camelCase -> "camel Case"
            .replaceAll("([A-Z]+)([A-Z][a-z])", "$1 $2") // UPPERCase -> "UPPER Case"
            .replaceAll("-", " ") // kebab-case
            .replaceAll("_", " ") // snake_case
            .toLowerCase();
        
        // Capitalize first letter
        if (!readable.isEmpty()) {
            readable = readable.substring(0, 1).toUpperCase() + readable.substring(1);
        }
        
        return readable;
    }
    
    /**
     * Get all discovered recipes
     */
    public List<RecipeInfo> getAllRecipes() {
        return new ArrayList<>(recipeCache.values());
    }
    
    /**
     * Get a specific recipe by name
     */
    public RecipeInfo getRecipe(String name) {
        return recipeCache.get(name);
    }
    
    /**
     * Check if a recipe exists
     */
    public boolean recipeExists(String name) {
        return recipeCache.containsKey(name);
    }
    
    /**
     * Get recipes for a list of names
     */
    public List<RecipeInfo> getRecipes(List<String> names) {
        List<RecipeInfo> result = new ArrayList<>();
        for (String name : names) {
            RecipeInfo info = recipeCache.get(name);
            if (info != null) {
                result.add(info);
            }
        }
        return result;
    }
    
    /**
     * Reload recipes from resources directory
     */
    public void reloadRecipes() {
        recipeCache.clear();
        loadRecipesFromResources();
    }
    
    public static class RecipeInfo {
        private final String fileName;
        private final String name;
        private final String description;
        private final String filePath;
        private final List<Recipe> recipes;
        
        public RecipeInfo(String fileName, String name, String description, String filePath, List<Recipe> recipes) {
            this.fileName = fileName;
            this.name = name;
            this.description = description;
            this.filePath = filePath;
            this.recipes = recipes;
        }
        
        public String getFileName() { return fileName; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getFilePath() { return filePath; }
        public List<Recipe> getRecipes() { return recipes; }
    }
}


