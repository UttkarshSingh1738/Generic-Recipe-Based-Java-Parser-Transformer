package com.recipe.api.services;

import com.recipe.api.dtos.JobDto;
import com.recipe.api.models.Project;
import com.recipe.api.models.Recipe;
import com.recipe.api.models.TransformationJob;
import com.recipe.api.repositories.JobRepository;
import com.recipe.api.repositories.ProjectRepository;
import com.recipe.api.repositories.RecipeRepository;
import com.recipe.api.services.engine.EnhancedPipelineService;
import com.recipe.api.services.engine.TransformationProgressCallback;
import com.recipe.api.services.storage.StorageService;
import com.recipe.api.services.RecipeDiscoveryService;
import com.recipe.api.services.DiffService;
import gst.api.MappingLoader;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class JobExecutionService {
    
    private static final Logger logger = LoggerFactory.getLogger(JobExecutionService.class);
    
    private final JobRepository jobRepository;
    private final ProjectRepository projectRepository;
    private final RecipeRepository recipeRepository;
    private final StorageService storageService;
    private final EnhancedPipelineService pipelineService;
    private final RecipeDiscoveryService recipeDiscoveryService;
    private final DiffService diffService;
    
    @Autowired
    public JobExecutionService(
            JobRepository jobRepository,
            ProjectRepository projectRepository,
            RecipeRepository recipeRepository,
            StorageService storageService,
            EnhancedPipelineService pipelineService,
            RecipeDiscoveryService recipeDiscoveryService,
            DiffService diffService) {
        this.jobRepository = jobRepository;
        this.projectRepository = projectRepository;
        this.recipeRepository = recipeRepository;
        this.storageService = storageService;
        this.pipelineService = pipelineService;
        this.recipeDiscoveryService = recipeDiscoveryService;
        this.diffService = diffService;
    }
    
    @Async
    public CompletableFuture<Void> executeJob(Long jobId) {
        logger.info("Starting execution of job {}", jobId);
        
        TransformationJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
        
        try {
            // Update job status to RUNNING (in separate transaction)
            updateJobStatus(jobId, TransformationJob.JobStatus.RUNNING, null);
            logger.info("Job {} status updated to RUNNING", jobId);
            
            // Load project and recipes
            Project project = job.getProject();
            List<String> recipeNames = List.of(job.getRecipeNames().split(","))
                    .stream()
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
            
            // Prepare input/output paths
            Path tempDir = Files.createTempDirectory("transformation-" + jobId);
            Path inputPath = tempDir.resolve("input");
            Files.createDirectories(inputPath);
            
            try {
                // Download project from storage
                String storagePrefix = project.getStoragePath() + "/" + project.getSourcePath();
                List<String> projectFiles = storageService.listFiles(storagePrefix);
                
                for (String filePath : projectFiles) {
                    try (InputStream is = storageService.retrieveFile(filePath)) {
                        String relativePath = filePath.substring(storagePrefix.length());
                        if (relativePath.startsWith("/")) {
                            relativePath = relativePath.substring(1);
                        }
                        Path localFile = inputPath.resolve(relativePath);
                        Files.createDirectories(localFile.getParent());
                        Files.copy(is, localFile);
                    }
                }
                
                // Execute each recipe INDEPENDENTLY against the original input
                int totalFilesTransformed = 0;
                int totalFilesFailed = 0;
                List<String> recipeOutputPaths = new ArrayList<>();
                
                TransformationProgressCallback callback = new TransformationProgressCallback() {
                    @Override
                    public void onFileProcessed(String fileName, boolean changed, int totalProcessed, int totalFiles) {
                        logger.debug("File processed: {} ({}/{})", fileName, totalProcessed, totalFiles);
                    }
                    
                    @Override
                    public void onRecipeApplied(String recipeName, String fileName, int matches) {
                        logger.debug("Recipe applied: {} on {} ({} matches)", recipeName, fileName, matches);
                    }
                    
                    @Override
                    public void onError(String fileName, String error) {
                        logger.error("Error processing {}: {}", fileName, error);
                    }
                    
                    @Override
                    public void onComplete(int totalFiles, int filesTransformed, int filesFailed) {
                        logger.info("Transformation complete: {} files, {} transformed, {} failed", 
                                totalFiles, filesTransformed, filesFailed);
                    }
                };
                
                for (String recipeName : recipeNames) {
                    logger.info("Executing recipe: {} independently against original input", recipeName);
                    
                    // Get recipe from discovery service (loads from resources)
                    RecipeDiscoveryService.RecipeInfo recipeInfo = recipeDiscoveryService.getRecipe(recipeName);
                    if (recipeInfo == null) {
                        logger.error("Recipe not found in resources: {}", recipeName);
                        continue;
                    }
                    
                    // Create output directory for this specific recipe
                    Path recipeOutputPath = tempDir.resolve("output").resolve(recipeName);
                    Files.createDirectories(recipeOutputPath);
                    
                    // Copy input to recipe-specific input (maintains original structure)
                    Path recipeInputPath = tempDir.resolve("recipe-input-" + recipeName);
                    copyDirectory(inputPath, recipeInputPath);
                    
                    // Execute transformation for this recipe
                    Path recipeLogFile = recipeOutputPath.getParent().resolve(recipeName + ".log");
                    try {
                        // Capture logs to file
                        EnhancedPipelineService.TransformationResult result = pipelineService.executeTransformationWithLogs(
                                recipeInfo.getRecipes(),
                                recipeInputPath,
                                recipeOutputPath,
                                recipeLogFile,
                                List.of(), // TODO: Support JAR paths
                                false, // TODO: Support matchDebug from job
                                callback
                        );
                        
                        totalFilesTransformed += result.getFilesTransformed();
                        totalFilesFailed += result.getFilesFailed();
                        
                        // Upload recipe-specific output to storage
                        String recipeOutputPrefix = "projects/" + project.getId() + "/jobs/" + jobId + "/recipes/" + recipeName;
                        uploadDirectoryToStorage(recipeOutputPath, recipeOutputPrefix);
                        recipeOutputPaths.add(recipeOutputPrefix);
                        
                       // Upload log file
                       if (Files.exists(recipeLogFile)) {
                           String logPath = "projects/" + project.getId() + "/jobs/" + jobId + "/logs/" + recipeName + ".log";
                           try (InputStream logStream = Files.newInputStream(recipeLogFile)) {
                               storageService.storeFile(logPath, logStream, "text/plain");
                           }
                           logger.info("Uploaded log file for recipe: {}", recipeName);
                       }
                        
                        // Generate and store diff for this recipe
                        try {
                            DiffService.DirectoryDiff diff = diffService.generateDirectoryDiff(recipeInputPath, recipeOutputPath);
                            String diffJson = new ObjectMapper().writeValueAsString(diff);
                            String diffPath = "projects/" + project.getId() + "/jobs/" + jobId + "/diffs/" + recipeName + ".json";
                            try (InputStream diffStream = new ByteArrayInputStream(diffJson.getBytes())) {
                                storageService.storeFile(diffPath, diffStream, "application/json");
                            }
                            logger.info("Generated diff for recipe {}: {} files changed, +{} -{}", 
                                    recipeName, diff.getChangedFiles(), diff.getTotalAdditions(), diff.getTotalDeletions());
                        } catch (Exception e) {
                            logger.warn("Failed to generate diff for recipe: " + recipeName, e);
                        }
                        
                       // Cleanup recipe input
                       deleteDirectory(recipeInputPath);
                       
                   } catch (Exception e) {
                       logger.error("Failed to execute recipe: " + recipeName, e);
                       totalFilesFailed++;
                   }
               }
               
               // Update job with results (in separate transaction)
               updateJobCompletion(jobId, totalFilesTransformed, totalFilesFailed, 
                       "projects/" + project.getId() + "/jobs/" + jobId + "/recipes");
               
               logger.info("Job {} completed successfully. Files transformed: {}, failed: {}", 
                       jobId, totalFilesTransformed, totalFilesFailed);
                
            } finally {
                // Cleanup temp directory
                deleteDirectory(tempDir);
            }
            
           } catch (Exception e) {
               logger.error("Job {} execution failed", jobId, e);
               updateJobStatus(jobId, TransformationJob.JobStatus.FAILED, e.getMessage());
           }
           
           return CompletableFuture.completedFuture(null);
       }
       
       @Transactional
       private void updateJobStatus(Long jobId, TransformationJob.JobStatus status, String errorMessage) {
           TransformationJob job = jobRepository.findById(jobId)
                   .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
           
           job.setStatus(status);
           
           if (status == TransformationJob.JobStatus.RUNNING && job.getStartedAt() == null) {
               job.setStartedAt(LocalDateTime.now());
           }
           
           if (status == TransformationJob.JobStatus.FAILED) {
               job.setCompletedAt(LocalDateTime.now());
               job.setErrorMessage(errorMessage);
           }
           
           jobRepository.save(job);
           jobRepository.flush(); // Force immediate persistence
           logger.info("Job {} status updated to {} in database", jobId, status);
       }
       
       @Transactional
       private void updateJobCompletion(Long jobId, int filesTransformed, int filesFailed, String outputPath) {
           TransformationJob job = jobRepository.findById(jobId)
                   .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));
           
           job.setStatus(TransformationJob.JobStatus.COMPLETED);
           job.setCompletedAt(LocalDateTime.now());
           job.setFilesTransformed(filesTransformed);
           job.setFilesFailed(filesFailed);
           job.setOutputPath(outputPath);
           
           jobRepository.save(job);
           jobRepository.flush(); // Force immediate persistence
           logger.info("Job {} marked as COMPLETED in database", jobId);
       }
    
    private void uploadDirectoryToStorage(Path localDir, String storagePrefix) throws IOException {
        Files.walk(localDir)
                .filter(Files::isRegularFile)
                .forEach(localFile -> {
                    try {
                        String relativePath = localDir.relativize(localFile).toString().replace('\\', '/');
                        String storagePath = storagePrefix + "/" + relativePath;
                        try (InputStream is = Files.newInputStream(localFile)) {
                            storageService.storeFile(storagePath, is, guessContentType(localFile.toString()));
                        }
                    } catch (IOException e) {
                        logger.error("Failed to upload file: " + localFile, e);
                    }
                });
    }
    
    private String guessContentType(String filename) {
        if (filename.endsWith(".java")) return "text/x-java-source";
        if (filename.endsWith(".xml")) return "application/xml";
        if (filename.endsWith(".properties")) return "text/plain";
        return "application/octet-stream";
    }
    
    private void copyDirectory(Path source, Path target) throws IOException {
        Files.walk(source)
                .forEach(sourcePath -> {
                    try {
                        Path targetPath = target.resolve(source.relativize(sourcePath));
                        if (Files.isDirectory(sourcePath)) {
                            Files.createDirectories(targetPath);
                        } else {
                            Files.createDirectories(targetPath.getParent());
                            Files.copy(sourcePath, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                        }
                    } catch (IOException e) {
                        logger.error("Failed to copy: " + sourcePath, e);
                    }
                });
    }
    
    private void deleteDirectory(Path dir) {
        try {
            if (!Files.exists(dir)) {
                return;
            }
            Files.walk(dir)
                    .sorted((a, b) -> -a.compareTo(b))
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            logger.warn("Failed to delete: " + path, e);
                        }
                    });
        } catch (IOException e) {
            logger.error("Failed to delete directory: " + dir, e);
        }
    }
}

