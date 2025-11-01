package com.recipe.api.repositories;

import com.recipe.api.models.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
    Optional<Recipe> findByName(String name);
    
    List<Recipe> findByIsPublicTrue();
    
    List<Recipe> findByCategory(String category);
    
    @Query("SELECT r FROM Recipe r WHERE r.tags LIKE %:tag% OR r.name LIKE %:search% OR r.description LIKE %:search%")
    List<Recipe> searchRecipes(@Param("tag") String tag, @Param("search") String search);
}

