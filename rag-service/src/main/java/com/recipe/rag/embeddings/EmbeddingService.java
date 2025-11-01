package com.recipe.rag.embeddings;

import com.theokanning.openai.embedding.Embedding;
import com.theokanning.openai.embedding.EmbeddingRequest;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "openai.api-key")
public class EmbeddingService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingService.class);
    private final OpenAiService openAiService;
    private final String model;
    
    public EmbeddingService(@Value("${openai.api-key}") String apiKey,
                           @Value("${openai.embedding-model:text-embedding-3-small}") String model) {
        this.model = model;
        this.openAiService = new OpenAiService(apiKey);
        logger.info("Initialized OpenAI embedding service with model: {}", model);
    }
    
    public List<Double> createEmbedding(String text) {
        try {
            EmbeddingRequest request = EmbeddingRequest.builder()
                    .model(model)
                    .input(List.of(text))
                    .build();
            
            List<Embedding> embeddings = openAiService.createEmbeddings(request).getData();
            
            if (!embeddings.isEmpty()) {
                return embeddings.get(0).getEmbedding();
            }
            
            return List.of();
        } catch (Exception e) {
            logger.error("Failed to create embedding", e);
            return List.of();
        }
    }
    
    public List<List<Double>> createEmbeddings(List<String> texts) {
        if (texts.isEmpty()) {
            return List.of();
        }
        
        try {
            EmbeddingRequest request = EmbeddingRequest.builder()
                    .model(model)
                    .input(texts)
                    .build();
            
            List<Embedding> embeddings = openAiService.createEmbeddings(request).getData();
            return embeddings.stream()
                    .map(Embedding::getEmbedding)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Failed to create embeddings", e);
            return List.of();
        }
    }
}

