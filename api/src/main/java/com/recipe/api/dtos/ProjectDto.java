package com.recipe.api.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectDto {
    private Long id;
    private String name;
    private String description;
    private String storagePath;
    private String sourcePath;
    private Integer fileCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

