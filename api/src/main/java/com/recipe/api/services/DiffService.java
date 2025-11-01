package com.recipe.api.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DiffService {
    
    private static final Logger logger = LoggerFactory.getLogger(DiffService.class);
    
    /**
     * Generate unified diff between two files
     */
    public FileDiff generateFileDiff(Path originalFile, Path transformedFile) throws IOException {
        String originalContent = "";
        String transformedContent = "";
        
        if (Files.exists(originalFile)) {
            originalContent = Files.readString(originalFile, StandardCharsets.UTF_8);
        }
        
        if (Files.exists(transformedFile)) {
            transformedContent = Files.readString(transformedFile, StandardCharsets.UTF_8);
        }
        
        return generateFileDiff(originalContent, transformedContent, 
                               originalFile.toString(), transformedFile.toString());
    }
    
    /**
     * Generate unified diff between two file contents
     */
    public FileDiff generateFileDiff(String originalContent, String transformedContent, 
                                     String originalPath, String transformedPath) {
        List<String> originalLines = originalContent.isEmpty() 
            ? new ArrayList<>() 
            : List.of(originalContent.split("\n", -1));
        List<String> transformedLines = transformedContent.isEmpty() 
            ? new ArrayList<>() 
            : List.of(transformedContent.split("\n", -1));
        
        // Generate line-by-line diff
        List<DiffLine> diffLines = new ArrayList<>();
        
        int maxLines = Math.max(originalLines.size(), transformedLines.size());
        int originalIdx = 0;
        int transformedIdx = 0;
        
        // Simple line-by-line comparison
        while (originalIdx < originalLines.size() || transformedIdx < transformedLines.size()) {
            String originalLine = originalIdx < originalLines.size() ? originalLines.get(originalIdx) : null;
            String transformedLine = transformedIdx < transformedLines.size() ? transformedLines.get(transformedIdx) : null;
            
            if (originalLine != null && transformedLine != null && originalLine.equals(transformedLine)) {
                // Unchanged line
                diffLines.add(new DiffLine(DiffLineType.CONTEXT, originalIdx + 1, transformedIdx + 1, originalLine));
                originalIdx++;
                transformedIdx++;
            } else if (originalLine != null && transformedLine != null) {
                // Modified line - check if it's a deletion followed by insertion
                // Try to match next original line with current transformed line
                boolean foundMatch = false;
                if (originalIdx + 1 < originalLines.size()) {
                    String nextOriginal = originalLines.get(originalIdx + 1);
                    if (nextOriginal.equals(transformedLine)) {
                        // Current original line was deleted
                        diffLines.add(new DiffLine(DiffLineType.DELETED, originalIdx + 1, -1, originalLine));
                        originalIdx++;
                        foundMatch = true;
                    }
                }
                
                if (!foundMatch) {
                    // Check if current original matches next transformed
                    if (transformedIdx + 1 < transformedLines.size()) {
                        String nextTransformed = transformedLines.get(transformedIdx + 1);
                        if (originalLine.equals(nextTransformed)) {
                            // Current transformed line was inserted
                            diffLines.add(new DiffLine(DiffLineType.INSERTED, -1, transformedIdx + 1, transformedLine));
                            transformedIdx++;
                            foundMatch = true;
                        }
                    }
                }
                
                if (!foundMatch) {
                    // Both lines are different - treat as deletion + insertion
                    diffLines.add(new DiffLine(DiffLineType.DELETED, originalIdx + 1, -1, originalLine));
                    diffLines.add(new DiffLine(DiffLineType.INSERTED, -1, transformedIdx + 1, transformedLine));
                    originalIdx++;
                    transformedIdx++;
                }
            } else if (originalLine != null) {
                // Deletion
                diffLines.add(new DiffLine(DiffLineType.DELETED, originalIdx + 1, -1, originalLine));
                originalIdx++;
            } else if (transformedLine != null) {
                // Insertion
                diffLines.add(new DiffLine(DiffLineType.INSERTED, -1, transformedIdx + 1, transformedLine));
                transformedIdx++;
            }
        }
        
        int additions = (int) diffLines.stream().filter(l -> l.getType() == DiffLineType.INSERTED).count();
        int deletions = (int) diffLines.stream().filter(l -> l.getType() == DiffLineType.DELETED).count();
        
        return new FileDiff(originalPath, transformedPath, diffLines, additions, deletions);
    }
    
    /**
     * Generate diff for entire directory structure
     */
    public DirectoryDiff generateDirectoryDiff(Path originalDir, Path transformedDir) throws IOException {
        List<FileDiff> fileDiffs = new ArrayList<>();
        int totalAdditions = 0;
        int totalDeletions = 0;
        int changedFiles = 0;
        
        // Get all Java files from both directories
        List<Path> originalFiles = new ArrayList<>();
        List<Path> transformedFiles = new ArrayList<>();
        
        if (Files.exists(originalDir)) {
            try (Stream<Path> stream = Files.walk(originalDir)) {
                originalFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
            }
        }
        
        if (Files.exists(transformedDir)) {
            try (Stream<Path> stream = Files.walk(transformedDir)) {
                transformedFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .collect(Collectors.toList());
            }
        }
        
        // Create a map of relative paths to absolute paths
        java.util.Map<String, Path> originalMap = originalFiles.stream()
            .collect(Collectors.toMap(
                p -> originalDir.relativize(p).toString().replace('\\', '/'),
                p -> p
            ));
        
        java.util.Map<String, Path> transformedMap = transformedFiles.stream()
            .collect(Collectors.toMap(
                p -> transformedDir.relativize(p).toString().replace('\\', '/'),
                p -> p
            ));
        
        // Get all unique file paths
        java.util.Set<String> allPaths = new java.util.HashSet<>(originalMap.keySet());
        allPaths.addAll(transformedMap.keySet());
        
        // Generate diff for each file
        for (String relativePath : allPaths) {
            Path originalFile = originalMap.get(relativePath);
            Path transformedFile = transformedMap.get(relativePath);
            
            try {
                FileDiff fileDiff = generateFileDiff(
                    originalFile != null ? originalFile : Path.of(""),
                    transformedFile != null ? transformedFile : Path.of("")
                );
                
                if (fileDiff.getAdditions() > 0 || fileDiff.getDeletions() > 0) {
                    fileDiff.setRelativePath(relativePath);
                    fileDiffs.add(fileDiff);
                    totalAdditions += fileDiff.getAdditions();
                    totalDeletions += fileDiff.getDeletions();
                    changedFiles++;
                }
            } catch (Exception e) {
                logger.warn("Failed to generate diff for file: " + relativePath, e);
            }
        }
        
        return new DirectoryDiff(originalDir.toString(), transformedDir.toString(), 
                               fileDiffs, totalAdditions, totalDeletions, changedFiles);
    }
    
    public enum DiffLineType {
        CONTEXT,    // Unchanged line
        DELETED,    // Line removed from original
        INSERTED    // Line added in transformed
    }
    
    public static class DiffLine {
        private DiffLineType type;
        private int originalLineNumber;
        private int transformedLineNumber;
        private String content;
        
        // Default constructor for Jackson
        public DiffLine() {}
        
        public DiffLine(DiffLineType type, int originalLineNumber, int transformedLineNumber, String content) {
            this.type = type;
            this.originalLineNumber = originalLineNumber;
            this.transformedLineNumber = transformedLineNumber;
            this.content = content;
        }
        
        public DiffLineType getType() { return type; }
        public void setType(DiffLineType type) { this.type = type; }
        public int getOriginalLineNumber() { return originalLineNumber; }
        public void setOriginalLineNumber(int originalLineNumber) { this.originalLineNumber = originalLineNumber; }
        public int getTransformedLineNumber() { return transformedLineNumber; }
        public void setTransformedLineNumber(int transformedLineNumber) { this.transformedLineNumber = transformedLineNumber; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
    
    public static class FileDiff {
        private String originalPath;
        private String transformedPath;
        private String relativePath;
        private List<DiffLine> lines;
        private int additions;
        private int deletions;
        
        // Default constructor for Jackson
        public FileDiff() {}
        
        public FileDiff(String originalPath, String transformedPath, List<DiffLine> lines, int additions, int deletions) {
            this.originalPath = originalPath;
            this.transformedPath = transformedPath;
            this.lines = lines;
            this.additions = additions;
            this.deletions = deletions;
        }
        
        public String getOriginalPath() { return originalPath; }
        public void setOriginalPath(String originalPath) { this.originalPath = originalPath; }
        public String getTransformedPath() { return transformedPath; }
        public void setTransformedPath(String transformedPath) { this.transformedPath = transformedPath; }
        public String getRelativePath() { return relativePath; }
        public void setRelativePath(String relativePath) { this.relativePath = relativePath; }
        public List<DiffLine> getLines() { return lines; }
        public void setLines(List<DiffLine> lines) { this.lines = lines; }
        public int getAdditions() { return additions; }
        public void setAdditions(int additions) { this.additions = additions; }
        public int getDeletions() { return deletions; }
        public void setDeletions(int deletions) { this.deletions = deletions; }
    }
    
    public static class DirectoryDiff {
        private String originalPath;
        private String transformedPath;
        private List<FileDiff> fileDiffs;
        private int totalAdditions;
        private int totalDeletions;
        private int changedFiles;
        
        // Default constructor for Jackson
        public DirectoryDiff() {}
        
        public DirectoryDiff(String originalPath, String transformedPath, List<FileDiff> fileDiffs, 
                           int totalAdditions, int totalDeletions, int changedFiles) {
            this.originalPath = originalPath;
            this.transformedPath = transformedPath;
            this.fileDiffs = fileDiffs;
            this.totalAdditions = totalAdditions;
            this.totalDeletions = totalDeletions;
            this.changedFiles = changedFiles;
        }
        
        public String getOriginalPath() { return originalPath; }
        public void setOriginalPath(String originalPath) { this.originalPath = originalPath; }
        public String getTransformedPath() { return transformedPath; }
        public void setTransformedPath(String transformedPath) { this.transformedPath = transformedPath; }
        public List<FileDiff> getFileDiffs() { return fileDiffs; }
        public void setFileDiffs(List<FileDiff> fileDiffs) { this.fileDiffs = fileDiffs; }
        public int getTotalAdditions() { return totalAdditions; }
        public void setTotalAdditions(int totalAdditions) { this.totalAdditions = totalAdditions; }
        public int getTotalDeletions() { return totalDeletions; }
        public void setTotalDeletions(int totalDeletions) { this.totalDeletions = totalDeletions; }
        public int getChangedFiles() { return changedFiles; }
        public void setChangedFiles(int changedFiles) { this.changedFiles = changedFiles; }
    }
}

