package com.recipe.rag;

import com.recipe.rag.documentation.DocumentationParser;
import com.recipe.rag.documentation.DocumentationParser.DocumentChunk;
import com.recipe.rag.embeddings.EmbeddingService;
import com.recipe.rag.retrieval.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Service
@ConditionalOnProperty(name = "openai.api-key")
public class KnowledgeBaseService {
    
    private static final Logger logger = LoggerFactory.getLogger(KnowledgeBaseService.class);
    
    private final EmbeddingService embeddingService;
    private final VectorStore vectorStore;
    private final DocumentationParser parser;
    
    @Value("${docs.path:../docs}")
    private String docsPath;
    
    @Autowired
    public KnowledgeBaseService(EmbeddingService embeddingService, 
                               VectorStore vectorStore,
                               DocumentationParser parser) {
        this.embeddingService = embeddingService;
        this.vectorStore = vectorStore;
        this.parser = parser;
    }
    
    @PostConstruct
    public void initializeKnowledgeBase() {
        logger.info("Initializing knowledge base from documentation...");
        
        try {
            Path docsDir = Paths.get(docsPath);
            
            // Parse all YAML documentation files
            List<DocumentChunk> allChunks = new ArrayList<>();
            
            if (Files.exists(docsDir)) {
                Path nodeTypesFile = docsDir.resolve("nodeTypes.yml");
                Path matchesFile = docsDir.resolve("matches.yml");
                Path actionsFile = docsDir.resolve("actions.yml");
                Path validatorsFile = docsDir.resolve("validators.yml");
                
                if (Files.exists(nodeTypesFile)) {
                    allChunks.addAll(parser.parseYamlDocumentation(nodeTypesFile));
                }
                if (Files.exists(matchesFile)) {
                    allChunks.addAll(parser.parseYamlDocumentation(matchesFile));
                }
                if (Files.exists(actionsFile)) {
                    allChunks.addAll(parser.parseYamlDocumentation(actionsFile));
                }
                if (Files.exists(validatorsFile)) {
                    allChunks.addAll(parser.parseYamlDocumentation(validatorsFile));
                }
            } else {
                logger.warn("Documentation directory not found: {}", docsPath);
                return;
            }
            
            // Create embeddings and store in vector database
            List<String> contents = allChunks.stream()
                    .map(DocumentChunk::getContent)
                    .toList();
            
            List<List<Double>> embeddings = embeddingService.createEmbeddings(contents);
            
            List<VectorStore.VectorDocument> documents = new ArrayList<>();
            for (int i = 0; i < allChunks.size(); i++) {
                DocumentChunk chunk = allChunks.get(i);
                List<Double> embedding = i < embeddings.size() ? embeddings.get(i) : List.of();
                
                documents.add(new VectorStore.VectorDocument(
                        chunk.getId(),
                        chunk.getContent(),
                        embedding,
                        chunk.getMetadata()
                ));
            }
            
            vectorStore.addDocuments(documents);
            logger.info("Knowledge base initialized with {} documents", documents.size());
            
        } catch (Exception e) {
            logger.error("Failed to initialize knowledge base", e);
        }
    }
    
    public List<VectorStore.VectorDocument> search(String query, int topK) {
        List<Double> queryEmbedding = embeddingService.createEmbedding(query);
        if (queryEmbedding.isEmpty()) {
            logger.warn("Failed to create embedding for query: {}", query);
            return List.of();
        }
        return vectorStore.search(queryEmbedding, topK);
    }
}

