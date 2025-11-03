package com.recipe.api.services.engine;

import com.fasterxml.jackson.databind.ObjectMapper;
import gst.api.Recipe;
import gst.api.RecipeContainer;
import gst.api.MappingLoader;
import gst.engine.Pipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
public class EnhancedPipelineService {
    
    private static final Logger logger = LoggerFactory.getLogger(EnhancedPipelineService.class);
    private final ExecutorService executorService;
    
    public EnhancedPipelineService() {
        // Use number of processors, but cap at 8 for reasonable resource usage
        int threads = Math.min(Runtime.getRuntime().availableProcessors(), 8);
        this.executorService = Executors.newFixedThreadPool(threads);
        logger.info("Initialized EnhancedPipelineService with {} worker threads", threads);
    }
    
    /**
     * Execute transformation with progress tracking and structured logging
     */
    public TransformationResult executeTransformation(
            List<Recipe> recipes,
            Path inputRoot,
            Path outputRoot,
            List<Path> jarPaths,
            boolean matchDebug,
            TransformationProgressCallback callback) {
        
        TransformationResult result = new TransformationResult();
        result.setTotalFiles(0);
        result.setFilesTransformed(0);
        result.setFilesFailed(0);
        
        try {
            // Count Java files first
            List<Path> javaFiles = Files.walk(inputRoot)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
            
            result.setTotalFiles(javaFiles.size());
            logger.info("Starting transformation of {} Java files", javaFiles.size());
            
            // Execute pipeline directly with Recipe objects (no re-serialization needed)
            Pipeline.run(recipes, inputRoot, outputRoot, jarPaths, matchDebug);
            
            // Count transformed files
            List<Path> transformedFiles = Files.walk(outputRoot)
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
            
            result.setFilesTransformed(transformedFiles.size());
            
            if (callback != null) {
                callback.onComplete(javaFiles.size(), transformedFiles.size(), 0);
            }
            
            logger.info("Transformation completed: {} files processed, {} transformed", 
                    javaFiles.size(), transformedFiles.size());
            
        } catch (Exception e) {
            logger.error("Transformation failed", e);
            result.setFilesFailed(result.getTotalFiles());
            result.setErrorMessage(e.getMessage());
            
            if (callback != null) {
                callback.onError("transformation", e.getMessage());
                callback.onComplete(result.getTotalFiles(), result.getFilesTransformed(), result.getFilesFailed());
            }
        }
        
        return result;
    }
    
    /**
     * Execute transformation with log capture to file
     */
    public TransformationResult executeTransformationWithLogs(
            List<Recipe> recipes,
            Path inputRoot,
            Path outputRoot,
            Path logFile,
            List<Path> jarPaths,
            boolean matchDebug,
            TransformationProgressCallback callback) {
        
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        
        try {
            // Create log file and redirect System.out/err
            Files.createDirectories(logFile.getParent());
            PrintStream logStream = new PrintStream(
                    Files.newOutputStream(logFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                    true); // autoFlush
            
            // Create a TeePrintStream to write to both original and log
            TeePrintStream teeOut = new TeePrintStream(originalOut, logStream);
            System.setOut(teeOut);
            System.setErr(teeOut);
            
            // Execute transformation
            TransformationResult result = executeTransformation(recipes, inputRoot, outputRoot, jarPaths, matchDebug, callback);
            
            return result;
            
        } catch (IOException e) {
            logger.error("Failed to create log file", e);
            // Fallback to regular execution without log capture
            return executeTransformation(recipes, inputRoot, outputRoot, jarPaths, matchDebug, callback);
        } finally {
            // Restore original streams
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }
    
    /**
     * Execute transformation asynchronously
     */
    public CompletableFuture<TransformationResult> executeTransformationAsync(
            List<Recipe> recipes,
            Path inputRoot,
            Path outputRoot,
            List<Path> jarPaths,
            boolean matchDebug,
            TransformationProgressCallback callback) {
        
        return CompletableFuture.supplyAsync(() -> 
            executeTransformation(recipes, inputRoot, outputRoot, jarPaths, matchDebug, callback),
            executorService
        );
    }
    
    /**
     * A PrintStream that writes to multiple streams
     */
    private static class TeePrintStream extends PrintStream {
        private final PrintStream original;
        private final PrintStream log;
        
        public TeePrintStream(PrintStream original, PrintStream log) {
            super(original);
            this.original = original;
            this.log = log;
        }
        
        @Override
        public void write(int b) {
            original.write(b);
            log.write(b);
        }
        
        @Override
        public void write(byte[] buf, int off, int len) {
            original.write(buf, off, len);
            log.write(buf, off, len);
        }
        
        @Override
        public void flush() {
            original.flush();
            log.flush();
        }
        
        @Override
        public void close() {
            // Don't close original stream
            log.close();
        }
    }
    
    public static class TransformationResult {
        private int totalFiles;
        private int filesTransformed;
        private int filesFailed;
        private String errorMessage;
        
        public int getTotalFiles() { return totalFiles; }
        public void setTotalFiles(int totalFiles) { this.totalFiles = totalFiles; }
        
        public int getFilesTransformed() { return filesTransformed; }
        public void setFilesTransformed(int filesTransformed) { this.filesTransformed = filesTransformed; }
        
        public int getFilesFailed() { return filesFailed; }
        public void setFilesFailed(int filesFailed) { this.filesFailed = filesFailed; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}

