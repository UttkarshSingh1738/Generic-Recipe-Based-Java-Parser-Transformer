package com.recipe.api.controllers;

import com.recipe.api.services.TransformationService;
import com.recipe.api.services.storage.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/jobs/{jobId}/output")
public class JobOutputController {
    
    private final TransformationService transformationService;
    private final StorageService storageService;
    
    @Autowired
    public JobOutputController(TransformationService transformationService, StorageService storageService) {
        this.transformationService = transformationService;
        this.storageService = storageService;
    }
    
    @GetMapping("/recipes")
    public ResponseEntity<List<String>> getRecipeOutputs(@PathVariable Long jobId) {
        return transformationService.getJobById(jobId)
                .map(job -> {
                    String basePath = "projects/" + job.getProjectId() + "/jobs/" + jobId + "/recipes/";
                    List<String> files = storageService.listFiles(basePath);
                    // Extract unique recipe names from paths
                    List<String> recipes = files.stream()
                            .map(path -> {
                                String relative = path.substring(basePath.length());
                                int firstSlash = relative.indexOf('/');
                                return firstSlash > 0 ? relative.substring(0, firstSlash) : relative;
                            })
                            .distinct()
                            .sorted()
                            .toList();
                    return ResponseEntity.ok(recipes);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/recipes/{recipeName}/**")
    public ResponseEntity<String> getFile(@PathVariable Long jobId, 
                                          @PathVariable String recipeName,
                                          @RequestParam(required = false) String path) {
        java.util.Optional<com.recipe.api.dtos.JobDto> jobOpt = transformationService.getJobById(jobId);
        if (!jobOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        com.recipe.api.dtos.JobDto job = jobOpt.get();
        String filePath = "projects/" + job.getProjectId() + "/jobs/" + jobId + "/recipes/" + recipeName;
        if (path != null && !path.isEmpty()) {
            filePath += "/" + path;
        }
        
        try {
            if (!storageService.fileExists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            
            try (InputStream is = storageService.retrieveFile(filePath)) {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                return ResponseEntity.ok().headers(headers).body(content);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/recipes/{recipeName}/files")
    public ResponseEntity<List<String>> listRecipeFiles(@PathVariable Long jobId, 
                                                         @PathVariable String recipeName) {
        return transformationService.getJobById(jobId)
                .map(job -> {
                    String recipePath = "projects/" + job.getProjectId() + "/jobs/" + jobId + "/recipes/" + recipeName;
                    List<String> files = storageService.listFiles(recipePath);
                    // Return relative paths
                    List<String> relativeFiles = files.stream()
                            .map(f -> f.substring(recipePath.length() + 1))
                            .filter(f -> f.endsWith(".java"))
                            .sorted()
                            .toList();
                    return ResponseEntity.ok(relativeFiles);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/logs/{recipeName}")
    public ResponseEntity<String> getLog(@PathVariable Long jobId, @PathVariable String recipeName) {
        java.util.Optional<com.recipe.api.dtos.JobDto> jobOpt = transformationService.getJobById(jobId);
        if (!jobOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        
        com.recipe.api.dtos.JobDto job = jobOpt.get();
        String logPath = "projects/" + job.getProjectId() + "/jobs/" + jobId + "/logs/" + recipeName + ".log";
        
        try {
            if (!storageService.fileExists(logPath)) {
                return ResponseEntity.notFound().build();
            }
            
            try (InputStream is = storageService.retrieveFile(logPath)) {
                String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.TEXT_PLAIN);
                return ResponseEntity.ok().headers(headers).body(content);
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}

