package com.recipe.api.services;

import com.recipe.api.dtos.JobDto;
import com.recipe.api.models.Project;
import com.recipe.api.models.TransformationJob;
import com.recipe.api.repositories.JobRepository;
import com.recipe.api.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransformationService {
    
    private final JobRepository jobRepository;
    private final ProjectRepository projectRepository;
    
    @Autowired
    public TransformationService(JobRepository jobRepository, ProjectRepository projectRepository) {
        this.jobRepository = jobRepository;
        this.projectRepository = projectRepository;
    }
    
    public List<JobDto> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public Optional<JobDto> getJobById(Long id) {
        return jobRepository.findById(id)
                .map(this::toDto);
    }
    
    public List<JobDto> getJobsByProject(Long projectId) {
        return jobRepository.findByProjectId(projectId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public List<JobDto> getJobsByStatus(TransformationJob.JobStatus status) {
        return jobRepository.findByStatus(status).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public JobDto createJob(Long projectId, List<String> recipeNames, Boolean matchDebug, 
                           JobExecutionService jobExecutionService) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        TransformationJob job = new TransformationJob();
        job.setProject(project);
        job.setRecipeNames(String.join(",", recipeNames));
        job.setStatus(TransformationJob.JobStatus.PENDING);
        
        TransformationJob saved = jobRepository.save(job);
        
        // Queue job for async execution
        if (jobExecutionService != null) {
            jobExecutionService.executeJob(saved.getId());
        }
        
        return toDto(saved);
    }
    
    @Transactional
    public Optional<JobDto> updateJobStatus(Long id, TransformationJob.JobStatus status) {
        return jobRepository.findById(id)
                .map(job -> {
                    job.setStatus(status);
                    if (status == TransformationJob.JobStatus.RUNNING && job.getStartedAt() == null) {
                        job.setStartedAt(LocalDateTime.now());
                    }
                    if (status == TransformationJob.JobStatus.COMPLETED || 
                        status == TransformationJob.JobStatus.FAILED ||
                        status == TransformationJob.JobStatus.CANCELLED) {
                        job.setCompletedAt(LocalDateTime.now());
                    }
                    return jobRepository.save(job);
                })
                .map(this::toDto);
    }
    
    private JobDto toDto(TransformationJob job) {
        return new JobDto(
                job.getId(),
                job.getProject().getId(),
                job.getProject().getName(),
                job.getRecipeNames(),
                job.getStatus(),
                job.getOutputPath(),
                job.getLogPath(),
                job.getFilesTransformed(),
                job.getFilesFailed(),
                job.getErrorMessage(),
                job.getCreatedAt(),
                job.getStartedAt(),
                job.getCompletedAt()
        );
    }
}

