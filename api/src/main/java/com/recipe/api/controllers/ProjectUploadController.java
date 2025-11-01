package com.recipe.api.controllers;

import com.recipe.api.models.Project;
import com.recipe.api.repositories.ProjectRepository;
import com.recipe.api.services.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@RestController
@RequestMapping("/api/projects")
public class ProjectUploadController {
    
    private static final Logger logger = LoggerFactory.getLogger(ProjectUploadController.class);
    
    private final ProjectRepository projectRepository;
    private final StorageService storageService;
    
    @Autowired
    public ProjectUploadController(ProjectRepository projectRepository, StorageService storageService) {
        this.projectRepository = projectRepository;
        this.storageService = storageService;
    }
    
    @PostMapping("/{projectId}/upload")
    public ResponseEntity<Map<String, Object>> uploadProject(
            @PathVariable("projectId") Long projectId,
            @RequestParam(value = "file", required = true) MultipartFile file) {
        
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found: " + projectId));
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            if (file.isEmpty()) {
                response.put("error", "File is empty");
                return ResponseEntity.badRequest().body(response);
            }
            
            String storagePrefix = "projects/" + projectId + "/source";
            int filesUploaded = 0;
            
            if (file.getOriginalFilename() != null && file.getOriginalFilename().endsWith(".zip")) {
                // Extract and upload ZIP file
                filesUploaded = extractAndUploadZip(file, storagePrefix);
            } else {
                // Upload single file
                String storagePath = storagePrefix + "/" + file.getOriginalFilename();
                storageService.storeFile(storagePath, file.getInputStream(), file.getContentType());
                filesUploaded = 1;
            }
            
            // Update project
            project.setStoragePath(storagePrefix);
            project.setSourcePath("");
            project.setFileCount(filesUploaded);
            projectRepository.save(project);
            
            response.put("message", "Project uploaded successfully");
            response.put("filesUploaded", filesUploaded);
            response.put("storagePath", storagePrefix);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Failed to upload project", e);
            response.put("error", "Upload failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    private int extractAndUploadZip(MultipartFile zipFile, String storagePrefix) throws IOException {
        int count = 0;
        try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    String entryName = entry.getName();
                    // Skip hidden files and directories
                    if (entryName.contains("__MACOSX") || entryName.startsWith(".")) {
                        continue;
                    }
                    
                    String storagePath = storagePrefix + "/" + entryName;
                    storageService.storeFile(storagePath, zis, guessContentType(entryName));
                    count++;
                }
                zis.closeEntry();
            }
        }
        return count;
    }
    
    private String guessContentType(String filename) {
        if (filename.endsWith(".java")) return "text/x-java-source";
        if (filename.endsWith(".xml")) return "application/xml";
        if (filename.endsWith(".properties")) return "text/plain";
        if (filename.endsWith(".json")) return "application/json";
        return "application/octet-stream";
    }
}

