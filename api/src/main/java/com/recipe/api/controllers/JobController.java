package com.recipe.api.controllers;

import com.recipe.api.dtos.CreateJobRequest;
import com.recipe.api.dtos.JobDto;
import com.recipe.api.models.TransformationJob;
import com.recipe.api.services.JobExecutionService;
import com.recipe.api.services.TransformationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
public class JobController {
    
    private final TransformationService transformationService;
    private final JobExecutionService jobExecutionService;
    
    @Autowired
    public JobController(TransformationService transformationService, JobExecutionService jobExecutionService) {
        this.transformationService = transformationService;
        this.jobExecutionService = jobExecutionService;
    }
    
    @GetMapping
    public ResponseEntity<List<JobDto>> getAllJobs() {
        return ResponseEntity.ok(transformationService.getAllJobs());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<JobDto> getJobById(@PathVariable Long id) {
        return transformationService.getJobById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<JobDto>> getJobsByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(transformationService.getJobsByProject(projectId));
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<JobDto>> getJobsByStatus(@PathVariable TransformationJob.JobStatus status) {
        return ResponseEntity.ok(transformationService.getJobsByStatus(status));
    }
    
    @PostMapping
    public ResponseEntity<JobDto> createJob(@Valid @RequestBody CreateJobRequest request) {
        JobDto created = transformationService.createJob(
                request.getProjectId(),
                request.getRecipeNames(),
                request.getMatchDebug(),
                jobExecutionService
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<JobDto> updateJobStatus(@PathVariable Long id,
                                                   @RequestParam TransformationJob.JobStatus status) {
        return transformationService.updateJobStatus(id, status)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

