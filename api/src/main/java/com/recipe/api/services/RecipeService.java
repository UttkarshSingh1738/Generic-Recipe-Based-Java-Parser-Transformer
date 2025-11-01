package com.recipe.api.services;

import com.recipe.api.dtos.RecipeDto;
import com.recipe.api.models.Recipe;
import com.recipe.api.repositories.RecipeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RecipeService {
    
    private final RecipeRepository recipeRepository;
    
    @Autowired
    public RecipeService(RecipeRepository recipeRepository) {
        this.recipeRepository = recipeRepository;
    }
    
    public List<RecipeDto> getAllRecipes() {
        return recipeRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public Optional<RecipeDto> getRecipeById(Long id) {
        return recipeRepository.findById(id)
                .map(this::toDto);
    }
    
    public Optional<RecipeDto> getRecipeByName(String name) {
        return recipeRepository.findByName(name)
                .map(this::toDto);
    }
    
    public List<RecipeDto> getPublicRecipes() {
        return recipeRepository.findByIsPublicTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<RecipeDto> searchRecipes(String searchTerm) {
        return recipeRepository.searchRecipes(searchTerm, searchTerm).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public RecipeDto createRecipe(RecipeDto dto) {
        Recipe recipe = toEntity(dto);
        Recipe saved = recipeRepository.save(recipe);
        return toDto(saved);
    }
    
    @Transactional
    public Optional<RecipeDto> updateRecipe(Long id, RecipeDto dto) {
        return recipeRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setDescription(dto.getDescription());
                    existing.setRecipeJson(dto.getRecipeJson());
                    existing.setVersion(dto.getVersion());
                    existing.setAuthor(dto.getAuthor());
                    existing.setTags(dto.getTags());
                    existing.setCategory(dto.getCategory());
                    existing.setIsPublic(dto.getIsPublic());
                    return recipeRepository.save(existing);
                })
                .map(this::toDto);
    }
    
    @Transactional
    public boolean deleteRecipe(Long id) {
        if (recipeRepository.existsById(id)) {
            recipeRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private RecipeDto toDto(Recipe recipe) {
        return new RecipeDto(
                recipe.getId(),
                recipe.getName(),
                recipe.getDescription(),
                recipe.getRecipeJson(),
                recipe.getVersion(),
                recipe.getAuthor(),
                recipe.getTags(),
                recipe.getCategory(),
                recipe.getIsPublic(),
                recipe.getCreatedAt(),
                recipe.getUpdatedAt()
        );
    }
    
    private Recipe toEntity(RecipeDto dto) {
        Recipe recipe = new Recipe();
        recipe.setName(dto.getName());
        recipe.setDescription(dto.getDescription());
        recipe.setRecipeJson(dto.getRecipeJson());
        recipe.setVersion(dto.getVersion());
        recipe.setAuthor(dto.getAuthor());
        recipe.setTags(dto.getTags());
        recipe.setCategory(dto.getCategory());
        recipe.setIsPublic(dto.getIsPublic() != null ? dto.getIsPublic() : false);
        return recipe;
    }
}

