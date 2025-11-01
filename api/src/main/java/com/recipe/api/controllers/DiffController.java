package com.recipe.api.controllers;

import com.recipe.api.services.DiffService;
import com.recipe.api.services.TransformationService;
import com.recipe.api.services.storage.StorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs/{jobId}/diffs")
public class DiffController {
    
    private final TransformationService transformationService;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;
    
    @Autowired
    public DiffController(TransformationService transformationService, StorageService storageService) {
        this.transformationService = transformationService;
        this.storageService = storageService;
        this.objectMapper = new ObjectMapper();
    }
    
    @GetMapping
    public ResponseEntity<Map<String, String>> getAllDiffs(@PathVariable Long jobId) {
        return transformationService.getJobById(jobId)
                .map(job -> {
                    Map<String, String> diffs = new HashMap<>();
                    String[] recipeNames = job.getRecipeNames().split(",");
                    
                    for (String recipeName : recipeNames) {
                        String recipeNameTrimmed = recipeName.trim();
                        String diffPath = "projects/" + job.getProjectId() + "/jobs/" + jobId + "/diffs/" + recipeNameTrimmed + ".json";
                        
                        try {
                            if (storageService.fileExists(diffPath)) {
                                diffs.put(recipeNameTrimmed, diffPath);
                            }
                        } catch (Exception e) {
                            // Skip if diff doesn't exist
                        }
                    }
                    
                    return ResponseEntity.ok(diffs);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/{recipeName}")
    public ResponseEntity<DiffService.DirectoryDiff> getDiff(@PathVariable Long jobId, @PathVariable String recipeName) {
        java.util.Optional<com.recipe.api.dtos.JobDto> jobOpt = transformationService.getJobById(jobId);
        if (!jobOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        com.recipe.api.dtos.JobDto job = jobOpt.get();
        String diffPath = "projects/" + job.getProjectId() + "/jobs/" + jobId + "/diffs/" + recipeName + ".json";
        
        try {
            if (!storageService.fileExists(diffPath)) {
                return ResponseEntity.notFound().build();
            }
            
            try (InputStream is = storageService.retrieveFile(diffPath)) {
                String diffJson = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                DiffService.DirectoryDiff diff = objectMapper.readValue(diffJson, DiffService.DirectoryDiff.class);
                return ResponseEntity.ok(diff);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

