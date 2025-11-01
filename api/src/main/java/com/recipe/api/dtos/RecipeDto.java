package com.recipe.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecipeDto {
    private Long id;
    private String name;
    private String description;
    private String recipeJson;
    private String version;
    private String author;
    private String tags;
    private String category;
    private Boolean isPublic;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

