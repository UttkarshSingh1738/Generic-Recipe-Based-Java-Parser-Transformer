package com.recipe.api.services.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Local file system implementation of StorageService for development/testing
 */
@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageService implements StorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalFileStorageService.class);
    
    @Value("${storage.local.base-path:./storage}")
    private String basePath;
    
    private Path storageRoot;
    
    @PostConstruct
    public void init() {
        try {
            storageRoot = Paths.get(basePath).toAbsolutePath().normalize();
            Files.createDirectories(storageRoot);
            logger.info("Local file storage initialized at: {}", storageRoot);
        } catch (IOException e) {
            logger.error("Failed to initialize local file storage", e);
            throw new RuntimeException("Storage service initialization failed", e);
        }
    }
    
    @Override
    public String storeFile(String storagePath, InputStream inputStream, String contentType) {
        try {
            Path filePath = storageRoot.resolve(storagePath);
            Files.createDirectories(filePath.getParent());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.debug("Stored file: {}", filePath);
            return storagePath;
        } catch (IOException e) {
            logger.error("Failed to store file: {}", storagePath, e);
            throw new RuntimeException("Failed to store file: " + storagePath, e);
        }
    }
    
    @Override
    public InputStream retrieveFile(String storagePath) {
        try {
            Path filePath = storageRoot.resolve(storagePath);
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            logger.error("Failed to retrieve file: {}", storagePath, e);
            throw new RuntimeException("Failed to retrieve file: " + storagePath, e);
        }
    }
    
    @Override
    public boolean deleteFile(String storagePath) {
        try {
            Path filePath = storageRoot.resolve(storagePath);
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                logger.debug("Deleted file: {}", filePath);
            }
            return deleted;
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", storagePath, e);
            return false;
        }
    }
    
    @Override
    public boolean fileExists(String storagePath) {
        Path filePath = storageRoot.resolve(storagePath);
        return Files.exists(filePath);
    }
    
    @Override
    public List<String> listFiles(String prefix) {
        List<String> files = new ArrayList<>();
        try {
            Path prefixPath = storageRoot.resolve(prefix);
            if (!Files.exists(prefixPath)) {
                return files;
            }
            
            try (Stream<Path> paths = Files.walk(prefixPath)) {
                paths.filter(Files::isRegularFile)
                        .forEach(path -> {
                            String relativePath = storageRoot.relativize(path).toString().replace('\\', '/');
                            files.add(relativePath);
                        });
            }
        } catch (IOException e) {
            logger.error("Failed to list files with prefix: {}", prefix, e);
            throw new RuntimeException("Failed to list files: " + prefix, e);
        }
        return files;
    }
    
    @Override
    public int copyDirectory(String sourcePrefix, String destinationPrefix) {
        List<String> sourceFiles = listFiles(sourcePrefix);
        int copied = 0;
        
        for (String sourcePath : sourceFiles) {
            try {
                String relativePath = sourcePath.substring(sourcePrefix.length());
                if (relativePath.startsWith("/")) {
                    relativePath = relativePath.substring(1);
                }
                String destPath = destinationPrefix + "/" + relativePath;
                
                Path sourceFilePath = storageRoot.resolve(sourcePath);
                Path destFilePath = storageRoot.resolve(destPath);
                
                Files.createDirectories(destFilePath.getParent());
                Files.copy(sourceFilePath, destFilePath, StandardCopyOption.REPLACE_EXISTING);
                copied++;
            } catch (IOException e) {
                logger.error("Failed to copy file from {} to {}", sourcePath, destinationPrefix, e);
            }
        }
        
        return copied;
    }
    
    @Override
    public int deleteDirectory(String prefix) {
        List<String> files = listFiles(prefix);
        int deleted = 0;
        
        for (String filePath : files) {
            if (deleteFile(filePath)) {
                deleted++;
            }
        }
        
        // Try to delete the directory itself if empty
        try {
            Path dirPath = storageRoot.resolve(prefix);
            if (Files.exists(dirPath)) {
                Files.deleteIfExists(dirPath);
            }
        } catch (IOException e) {
            logger.debug("Could not delete directory: {}", prefix);
        }
        
        return deleted;
    }
}

