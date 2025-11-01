package com.recipe.api.dtos;

import com.recipe.api.models.TransformationJob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {
    private Long id;
    private Long projectId;
    private String projectName;
    private String recipeNames;
    private TransformationJob.JobStatus status;
    private String outputPath;
    private String logPath;
    private Integer filesTransformed;
    private Integer filesFailed;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}

