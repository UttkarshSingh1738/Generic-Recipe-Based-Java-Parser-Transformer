package com.recipe.rag.retrieval;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class VectorStore {
    
    private static final Logger logger = LoggerFactory.getLogger(VectorStore.class);
    
    // In-memory vector store - can be replaced with Chroma, Weaviate, etc.
    private final Map<String, VectorDocument> documents = new ConcurrentHashMap<>();
    
    public void addDocument(String id, String content, List<Double> embedding, Map<String, Object> metadata) {
        documents.put(id, new VectorDocument(id, content, embedding, metadata));
        logger.debug("Added document to vector store: {}", id);
    }
    
    public void addDocuments(List<VectorDocument> docs) {
        for (VectorDocument doc : docs) {
            documents.put(doc.id, doc);
        }
        logger.info("Added {} documents to vector store", docs.size());
    }
    
    public List<VectorDocument> search(List<Double> queryEmbedding, int topK) {
        return search(queryEmbedding, topK, null);
    }
    
    public List<VectorDocument> search(List<Double> queryEmbedding, int topK, String sourceFilter) {
        List<SimilarityResult> results = new ArrayList<>();
        
        for (VectorDocument doc : documents.values()) {
            if (sourceFilter != null && !doc.metadata.getOrDefault("source", "").equals(sourceFilter)) {
                continue;
            }
            
            double similarity = cosineSimilarity(queryEmbedding, doc.embedding);
            results.add(new SimilarityResult(doc, similarity));
        }
        
        return results.stream()
                .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
                .limit(topK)
                .map(r -> r.document)
                .collect(Collectors.toList());
    }
    
    private double cosineSimilarity(List<Double> vec1, List<Double> vec2) {
        if (vec1.size() != vec2.size() || vec1.isEmpty()) {
            return 0.0;
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < vec1.size(); i++) {
            dotProduct += vec1.get(i) * vec2.get(i);
            norm1 += vec1.get(i) * vec1.get(i);
            norm2 += vec2.get(i) * vec2.get(i);
        }
        
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    public int size() {
        return documents.size();
    }
    
    public void clear() {
        documents.clear();
        logger.info("Vector store cleared");
    }
    
    public static class VectorDocument {
        public final String id;
        public final String content;
        public final List<Double> embedding;
        public final Map<String, Object> metadata;
        
        public VectorDocument(String id, String content, List<Double> embedding, Map<String, Object> metadata) {
            this.id = id;
            this.content = content;
            this.embedding = embedding;
            this.metadata = metadata;
        }
    }
    
    private static class SimilarityResult {
        final VectorDocument document;
        final double similarity;
        
        SimilarityResult(VectorDocument document, double similarity) {
            this.document = document;
            this.similarity = similarity;
        }
    }
}

