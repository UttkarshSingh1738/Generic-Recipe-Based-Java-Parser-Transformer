package com.recipe.api.controllers;

import com.recipe.api.dtos.RecipeDto;
import com.recipe.api.services.RecipeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes/generate")
@ConditionalOnBean(name = "recipeGenerationService")
public class RecipeGenerationController {
    
    private final com.recipe.rag.generation.RecipeGenerationService generationService;
    private final RecipeService recipeService;
    
    @Autowired
    public RecipeGenerationController(
            com.recipe.rag.generation.RecipeGenerationService generationService, 
            RecipeService recipeService) {
        this.generationService = generationService;
        this.recipeService = recipeService;
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> generateRecipe(@RequestBody Map<String, String> request) {
        String intent = request.get("intent");
        if (intent == null || intent.trim().isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Intent is required");
            return ResponseEntity.badRequest().body(error);
        }
        
        try {
            com.recipe.rag.generation.RecipeGenerationService.GeneratedRecipe generated = generationService.generateRecipe(intent);
            
            Map<String, Object> response = new HashMap<>();
            response.put("name", generated.getName());
            response.put("description", generated.getDescription());
            response.put("recipeJson", generated.getRecipeJson());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Generation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/save")
    public ResponseEntity<RecipeDto> generateAndSaveRecipe(@Valid @RequestBody Map<String, String> request) {
        String intent = request.get("intent");
        String author = request.getOrDefault("author", "system");
        String category = request.getOrDefault("category", "generated");
        
        if (intent == null || intent.trim().isEmpty()) {
            throw new IllegalArgumentException("Intent is required");
        }
        
        com.recipe.rag.generation.RecipeGenerationService.GeneratedRecipe generated = generationService.generateRecipe(intent);
        
        // Create RecipeDto and save
        RecipeDto dto = new RecipeDto();
        dto.setName(generated.getName());
        dto.setDescription(generated.getDescription());
        dto.setRecipeJson(generated.getRecipeJson());
        dto.setAuthor(author);
        dto.setCategory(category);
        dto.setIsPublic(false);
        
        RecipeDto saved = recipeService.createRecipe(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}

