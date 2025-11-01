package com.recipe.api.services.storage;

import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "minio", matchIfMissing = false)
public class MinioStorageService implements StorageService {
    
    private static final Logger logger = LoggerFactory.getLogger(MinioStorageService.class);
    
    private MinioClient minioClient;
    
    @Value("${storage.minio.endpoint:http://localhost:9000}")
    private String endpoint;
    
    @Value("${storage.minio.access-key:minioadmin}")
    private String accessKey;
    
    @Value("${storage.minio.secret-key:minioadmin}")
    private String secretKey;
    
    @Value("${storage.minio.bucket:recipe-transformer}")
    private String bucketName;
    
    @PostConstruct
    public void init() {
        try {
            minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            
            // Ensure bucket exists
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build());
            
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build());
                logger.info("Created bucket: {}", bucketName);
            }
            
            logger.info("MinIO storage service initialized successfully");
        } catch (Exception e) {
            logger.error("Failed to initialize MinIO storage service", e);
            throw new RuntimeException("Storage service initialization failed", e);
        }
    }
    
    @Override
    public String storeFile(String storagePath, InputStream inputStream, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storagePath)
                    .stream(inputStream, -1, 10485760) // 10MB part size
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build());
            
            logger.debug("Stored file: {}", storagePath);
            return storagePath;
        } catch (Exception e) {
            logger.error("Failed to store file: {}", storagePath, e);
            throw new RuntimeException("Failed to store file: " + storagePath, e);
        }
    }
    
    @Override
    public InputStream retrieveFile(String storagePath) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storagePath)
                    .build());
        } catch (Exception e) {
            logger.error("Failed to retrieve file: {}", storagePath, e);
            throw new RuntimeException("Failed to retrieve file: " + storagePath, e);
        }
    }
    
    @Override
    public boolean deleteFile(String storagePath) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storagePath)
                    .build());
            logger.debug("Deleted file: {}", storagePath);
            return true;
        } catch (Exception e) {
            logger.error("Failed to delete file: {}", storagePath, e);
            return false;
        }
    }
    
    @Override
    public boolean fileExists(String storagePath) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(storagePath)
                    .build());
            return true;
        } catch (ErrorResponseException e) {
            if (e.errorResponse().code().equals("NoSuchKey")) {
                return false;
            }
            logger.error("Error checking file existence: {}", storagePath, e);
            throw new RuntimeException("Failed to check file existence: " + storagePath, e);
        } catch (Exception e) {
            logger.error("Failed to check file existence: {}", storagePath, e);
            throw new RuntimeException("Failed to check file existence: " + storagePath, e);
        }
    }
    
    @Override
    public List<String> listFiles(String prefix) {
        List<String> files = new ArrayList<>();
        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix(prefix)
                            .recursive(true)
                            .build());
            
            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir()) {
                    files.add(item.objectName());
                }
            }
        } catch (Exception e) {
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
                String destPath = destinationPrefix + relativePath;
                
                // Read source file
                InputStream sourceStream = retrieveFile(sourcePath);
                byte[] content = sourceStream.readAllBytes();
                sourceStream.close();
                
                // Write to destination
                storeFile(destPath, new ByteArrayInputStream(content), "application/octet-stream");
                copied++;
            } catch (Exception e) {
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
        
        return deleted;
    }
}

