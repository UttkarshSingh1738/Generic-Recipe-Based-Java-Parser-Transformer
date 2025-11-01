package com.recipe.api.dtos;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateJobRequest {
    @NotNull(message = "Project ID is required")
    private Long projectId;
    
    @NotEmpty(message = "At least one recipe name is required")
    private List<String> recipeNames;
    
    private Boolean matchDebug = false;
}

