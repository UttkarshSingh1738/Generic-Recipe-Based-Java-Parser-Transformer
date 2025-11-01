package com.recipe.api.controllers;

import com.recipe.api.services.RecipeDiscoveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/recipes/discovery")
public class RecipeDiscoveryController {
    
    private static final Logger logger = LoggerFactory.getLogger(RecipeDiscoveryController.class);
    
    private final RecipeDiscoveryService recipeDiscoveryService;
    
    @Autowired
    public RecipeDiscoveryController(RecipeDiscoveryService recipeDiscoveryService) {
        this.recipeDiscoveryService = recipeDiscoveryService;
    }
    
    @GetMapping
    public ResponseEntity<List<RecipeDiscoveryService.RecipeInfo>> getAllDiscoveredRecipes() {
        return ResponseEntity.ok(recipeDiscoveryService.getAllRecipes());
    }
    
    @GetMapping("/{name}")
    public ResponseEntity<RecipeDiscoveryService.RecipeInfo> getRecipe(@PathVariable String name) {
        RecipeDiscoveryService.RecipeInfo recipe = recipeDiscoveryService.getRecipe(name);
        if (recipe != null) {
            return ResponseEntity.ok(recipe);
        }
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping("/{name}/content")
    public ResponseEntity<String> getRecipeContent(@PathVariable String name) {
        RecipeDiscoveryService.RecipeInfo recipe = recipeDiscoveryService.getRecipe(name);
        if (recipe == null) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            // Read the raw JSON file content
            java.nio.file.Path filePath = java.nio.file.Paths.get(recipe.getFilePath());
            if (!java.nio.file.Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            String content = java.nio.file.Files.readString(filePath);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(content);
        } catch (Exception e) {
            logger.error("Failed to read recipe content for: " + name, e);
            return ResponseEntity.status(500)
                    .body("{\"error\":\"Failed to read recipe content: " + e.getMessage() + "\"}");
        }
    }
    
    @PostMapping("/reload")
    public ResponseEntity<Void> reloadRecipes() {
        recipeDiscoveryService.reloadRecipes();
        return ResponseEntity.ok().build();
    }
}
