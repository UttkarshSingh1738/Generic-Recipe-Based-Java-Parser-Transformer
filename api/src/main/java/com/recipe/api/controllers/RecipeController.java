package com.recipe.api.controllers;

import com.recipe.api.dtos.RecipeDto;
import com.recipe.api.services.RecipeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    
    private final RecipeService recipeService;
    
    @Autowired
    public RecipeController(RecipeService recipeService) {
        this.recipeService = recipeService;
    }
    
    @GetMapping
    public ResponseEntity<List<RecipeDto>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }
    
    @GetMapping("/public")
    public ResponseEntity<List<RecipeDto>> getPublicRecipes() {
        return ResponseEntity.ok(recipeService.getPublicRecipes());
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<RecipeDto>> searchRecipes(@RequestParam String q) {
        return ResponseEntity.ok(recipeService.searchRecipes(q));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<RecipeDto> getRecipeById(@PathVariable Long id) {
        return recipeService.getRecipeById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/name/{name}")
    public ResponseEntity<RecipeDto> getRecipeByName(@PathVariable String name) {
        return recipeService.getRecipeByName(name)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<RecipeDto> createRecipe(@Valid @RequestBody RecipeDto recipeDto) {
        RecipeDto created = recipeService.createRecipe(recipeDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RecipeDto> updateRecipe(@PathVariable Long id, 
                                                   @Valid @RequestBody RecipeDto recipeDto) {
        return recipeService.updateRecipe(id, recipeDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRecipe(@PathVariable Long id) {
        if (recipeService.deleteRecipe(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

