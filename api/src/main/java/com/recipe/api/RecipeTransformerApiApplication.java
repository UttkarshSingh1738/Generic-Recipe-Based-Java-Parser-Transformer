package com.recipe.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.beans.factory.annotation.Autowired;
import com.recipe.api.repositories.ProjectRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@SpringBootApplication
@EnableAsync
@ComponentScan(basePackages = {"com.recipe.api", "com.recipe.rag"})
public class RecipeTransformerApiApplication {

    private static final Logger logger = LoggerFactory.getLogger(RecipeTransformerApiApplication.class);

    @Autowired(required = false)
    private ProjectRepository projectRepository;

    public static void main(String[] args) {
        SpringApplication.run(RecipeTransformerApiApplication.class, args);
    }
    
    @EventListener(ApplicationReadyEvent.class)
    public void cleanupOrphanedStorage() {
        logger.info("Checking for orphaned storage files...");
        
        try {
            // For H2 in-memory database, clear all storage on startup since DB resets
            String datasourceUrl = System.getenv().getOrDefault("spring.datasource.url", "h2:mem");
            if (datasourceUrl.contains("h2:mem") || datasourceUrl.contains("jdbc:h2:mem")) {
                logger.info("H2 in-memory database detected - clearing all storage on startup");
                clearAllStorage();
            } else {
                logger.info("Persistent database detected - storage files retained");
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup orphaned storage", e);
        }
    }
    
    private void clearAllStorage() {
        try {
            Path storagePath = Paths.get("./storage/projects");
            if (Files.exists(storagePath)) {
                try (Stream<Path> paths = Files.walk(storagePath)) {
                    paths.sorted((a, b) -> -a.compareTo(b)) // Reverse order to delete children first
                         .forEach(path -> {
                             try {
                                 Files.deleteIfExists(path);
                             } catch (IOException e) {
                                 logger.warn("Failed to delete: {}", path, e);
                             }
                         });
                }
                // Recreate the projects directory
                Files.createDirectories(storagePath);
                logger.info("Storage cleared successfully");
            }
        } catch (IOException e) {
            logger.error("Failed to clear storage", e);
        }
    }
}

