package com.recipe.api.services.storage;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;

public interface StorageService {
    
    /**
     * Store a file at the given path
     * @param storagePath The path in storage (e.g., "projects/123/source/MyClass.java")
     * @param inputStream The file content
     * @param contentType The MIME type of the file
     * @return The full storage path
     */
    String storeFile(String storagePath, InputStream inputStream, String contentType);
    
    /**
     * Retrieve a file from storage
     * @param storagePath The path in storage
     * @return InputStream of the file content
     */
    InputStream retrieveFile(String storagePath);
    
    /**
     * Delete a file from storage
     * @param storagePath The path in storage
     * @return true if deleted successfully
     */
    boolean deleteFile(String storagePath);
    
    /**
     * Check if a file exists in storage
     * @param storagePath The path in storage
     * @return true if file exists
     */
    boolean fileExists(String storagePath);
    
    /**
     * List all files in a directory prefix
     * @param prefix The directory prefix (e.g., "projects/123/")
     * @return List of file paths
     */
    List<String> listFiles(String prefix);
    
    /**
     * Copy a directory recursively
     * @param sourcePrefix Source directory prefix
     * @param destinationPrefix Destination directory prefix
     * @return Number of files copied
     */
    int copyDirectory(String sourcePrefix, String destinationPrefix);
    
    /**
     * Delete a directory and all its contents
     * @param prefix The directory prefix
     * @return Number of files deleted
     */
    int deleteDirectory(String prefix);
}

