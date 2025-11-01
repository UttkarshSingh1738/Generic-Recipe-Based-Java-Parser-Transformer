package com.recipe.rag.documentation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@Component
public class DocumentationParser {
    
    private static final Logger logger = LoggerFactory.getLogger(DocumentationParser.class);
    
    public List<DocumentChunk> parseYamlDocumentation(Path yamlFile) {
        List<DocumentChunk> chunks = new ArrayList<>();
        
        try (InputStream is = Files.newInputStream(yamlFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = yaml.load(is);
            
            String fileName = yamlFile.getFileName().toString();
            if (fileName.contains("nodeTypes")) {
                chunks.addAll(parseNodeTypes(data));
            } else if (fileName.contains("matches")) {
                chunks.addAll(parseMatches(data));
            } else if (fileName.contains("actions")) {
                chunks.addAll(parseActions(data));
            } else if (fileName.contains("validators")) {
                chunks.addAll(parseValidators(data));
            }
            
        } catch (IOException e) {
            logger.error("Failed to parse YAML file: " + yamlFile, e);
        }
        
        return chunks;
    }
    
    private List<DocumentChunk> parseNodeTypes(Map<String, Object> data) {
        List<DocumentChunk> chunks = new ArrayList<>();
        
        // nodeTypes.yml is a list at root level
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> nodeTypes;
        if (data instanceof java.util.List) {
            nodeTypes = (List<Map<String, Object>>) data;
        } else {
            // Try to get first value if it's a map
            Object firstValue = data.values().isEmpty() ? null : data.values().iterator().next();
            if (firstValue instanceof List) {
                nodeTypes = (List<Map<String, Object>>) firstValue;
            } else {
                return chunks; // Can't parse
            }
        }
        
        for (Map<String, Object> nodeType : nodeTypes) {
            String type = (String) nodeType.get("nodeType");
            String description = (String) nodeType.get("description");
            String example = (String) nodeType.get("example");
            
            StringBuilder content = new StringBuilder();
            content.append("NodeType: ").append(type).append("\n");
            content.append("Description: ").append(description).append("\n");
            if (example != null) {
                content.append("Example:\n").append(example).append("\n");
            }
            
            chunks.add(new DocumentChunk(
                    "nodeType:" + type,
                    content.toString(),
                    "nodeTypes",
                    Map.of("nodeType", type)
            ));
        }
        
        return chunks;
    }
    
    private List<DocumentChunk> parseMatches(Map<String, Object> data) {
        List<DocumentChunk> chunks = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> matches = (List<Map<String, Object>>) data.get("matches");
        
        for (Map<String, Object> match : matches) {
            String key = (String) match.get("key");
            String description = (String) match.get("description");
            String example = (String) match.get("example");
            @SuppressWarnings("unchecked")
            List<String> appliesTo = (List<String>) match.get("appliesTo");
            
            StringBuilder content = new StringBuilder();
            content.append("Match Key: ").append(key).append("\n");
            content.append("Description: ").append(description).append("\n");
            if (appliesTo != null) {
                content.append("Applies to: ").append(String.join(", ", appliesTo)).append("\n");
            }
            if (example != null) {
                content.append("Example:\n").append(example).append("\n");
            }
            
            chunks.add(new DocumentChunk(
                    "match:" + key,
                    content.toString(),
                    "matches",
                    Map.of("matchKey", key, "appliesTo", appliesTo != null ? appliesTo : List.of())
            ));
        }
        
        return chunks;
    }
    
    private List<DocumentChunk> parseActions(Map<String, Object> data) {
        List<DocumentChunk> chunks = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> actions = (List<Map<String, Object>>) data.get("actions");
        
        for (Map<String, Object> action : actions) {
            String key = (String) action.get("key");
            String description = (String) action.get("description");
            String example = (String) action.get("example");
            @SuppressWarnings("unchecked")
            List<String> appliesTo = (List<String>) action.get("appliesTo");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> parameters = (List<Map<String, Object>>) action.get("parameters");
            
            StringBuilder content = new StringBuilder();
            content.append("Action: ").append(key).append("\n");
            content.append("Description: ").append(description).append("\n");
            if (appliesTo != null) {
                content.append("Applies to: ").append(String.join(", ", appliesTo)).append("\n");
            }
            if (parameters != null) {
                content.append("Parameters:\n");
                for (Map<String, Object> param : parameters) {
                    content.append("  - ").append(param.get("name"))
                           .append(": ").append(param.get("description")).append("\n");
                }
            }
            if (example != null) {
                content.append("Example:\n").append(example).append("\n");
            }
            
            chunks.add(new DocumentChunk(
                    "action:" + key,
                    content.toString(),
                    "actions",
                    Map.of("actionKey", key, "appliesTo", appliesTo != null ? appliesTo : List.of())
            ));
        }
        
        return chunks;
    }
    
    private List<DocumentChunk> parseValidators(Map<String, Object> data) {
        List<DocumentChunk> chunks = new ArrayList<>();
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> validators = (List<Map<String, Object>>) data.get("validators");
        
        for (Map<String, Object> validator : validators) {
            String name = (String) validator.get("name");
            String description = (String) validator.get("description");
            String example = (String) validator.get("example");
            
            StringBuilder content = new StringBuilder();
            content.append("Validator: ").append(name).append("\n");
            content.append("Description: ").append(description).append("\n");
            if (example != null) {
                content.append("Example:\n").append(example).append("\n");
            }
            
            chunks.add(new DocumentChunk(
                    "validator:" + name,
                    content.toString(),
                    "validators",
                    Map.of("validatorName", name)
            ));
        }
        
        return chunks;
    }
    
    public static class DocumentChunk {
        private final String id;
        private final String content;
        private final String source;
        private final Map<String, Object> metadata;
        
        public DocumentChunk(String id, String content, String source, Map<String, Object> metadata) {
            this.id = id;
            this.content = content;
            this.source = source;
            this.metadata = metadata;
        }
        
        public String getId() { return id; }
        public String getContent() { return content; }
        public String getSource() { return source; }
        public Map<String, Object> getMetadata() { return metadata; }
    }
}

