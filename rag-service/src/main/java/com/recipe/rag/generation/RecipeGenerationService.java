package com.recipe.rag.generation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.rag.KnowledgeBaseService;
import com.recipe.rag.retrieval.VectorStore;
import com.theokanning.openai.completion.chat.*;
import com.theokanning.openai.service.OpenAiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service("recipeGenerationService")
@ConditionalOnProperty(name = "openai.api-key")
public class RecipeGenerationService {
    
    private static final Logger logger = LoggerFactory.getLogger(RecipeGenerationService.class);
    
    private final KnowledgeBaseService knowledgeBase;
    private final ObjectMapper objectMapper;
    private final OpenAiService openAiService;
    private final String model;
    
    @Autowired
    public RecipeGenerationService(
            KnowledgeBaseService knowledgeBase,
            @Value("${openai.api-key}") String apiKey,
            @Value("${openai.model:gpt-4}") String model) {
        this.knowledgeBase = knowledgeBase;
        this.objectMapper = new ObjectMapper();
        this.model = model;
        
        this.openAiService = new OpenAiService(apiKey);
        logger.info("Initialized OpenAI service with model: {}", model);
    }
    
    public GeneratedRecipe generateRecipe(String userIntent) {
        try {
            // Retrieve relevant documentation chunks
            List<VectorStore.VectorDocument> relevantDocs = knowledgeBase.search(userIntent, 10);
            
            // Build context from retrieved documents
            String context = relevantDocs.stream()
                    .map(doc -> doc.content)
                    .collect(Collectors.joining("\n\n---\n\n"));
            
            // Build prompt
            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(userIntent, context);
            
            // Call OpenAI
            ChatCompletionRequest request = ChatCompletionRequest.builder()
                    .model(model)
                    .messages(List.of(
                            new ChatMessage(ChatMessageRole.SYSTEM.value(), systemPrompt),
                            new ChatMessage(ChatMessageRole.USER.value(), userPrompt)
                    ))
                    .temperature(0.3)
                    .build();
            
            ChatCompletionResult result = openAiService.createChatCompletion(request);
            String response = result.getChoices().get(0).getMessage().getContent();
            
            // Parse response
            return parseRecipeResponse(response);
            
        } catch (Exception e) {
            logger.error("Failed to generate recipe", e);
            throw new RuntimeException("Recipe generation failed: " + e.getMessage(), e);
        }
    }
    
    private String buildSystemPrompt() {
        return """
            You are an expert in Java code transformation recipes. Your task is to generate valid JSON recipe configurations
            for the Recipe-Based Java Parser Transformer.
            
            The recipe format follows this structure:
            {
              "recipes": [{
                "name": "RecipeName",
                "description": "Description of what this recipe does",
                "rollbackOnError": "OptionalValidatorName",
                "imports": {
                  "add": ["fully.qualified.Type"],
                  "remove": ["old.qualified.Type"]
                },
                "steps": [{
                  "match": {
                    "nodeType": "NodeTypeName",
                    // Additional match criteria...
                  },
                  "actions": [{
                    "actionName": {
                      // Action parameters...
                    }
                  }]
                }]
              }]
            }
            
            Return ONLY valid JSON, no markdown code blocks, no explanations.
            """;
    }
    
    private String buildUserPrompt(String userIntent, String context) {
        return String.format("""
            User Intent: %s
            
            Available Documentation Context:
            %s
            
            Generate a recipe JSON that accomplishes the user's intent. Use the documentation to understand
            which nodeTypes, matches, and actions are available. Return ONLY the JSON recipe.
            """, userIntent, context);
    }
    
    private GeneratedRecipe parseRecipeResponse(String response) {
        try {
            // Remove markdown code blocks if present
            String json = response.trim();
            if (json.startsWith("```json")) {
                json = json.substring(7);
            }
            if (json.startsWith("```")) {
                json = json.substring(3);
            }
            if (json.endsWith("```")) {
                json = json.substring(0, json.length() - 3);
            }
            json = json.trim();
            
            // Parse JSON
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> recipeData = objectMapper.readValue(json, java.util.Map.class);
            
            String name = (String) ((java.util.Map<?, ?>) ((java.util.List<?>) recipeData.get("recipes")).get(0)).get("name");
            String description = (String) ((java.util.Map<?, ?>) ((java.util.List<?>) recipeData.get("recipes")).get(0)).get("description");
            
            return new GeneratedRecipe(name, description, json);
            
        } catch (Exception e) {
            logger.error("Failed to parse recipe response", e);
            throw new RuntimeException("Failed to parse generated recipe: " + e.getMessage(), e);
        }
    }
    
    public static class GeneratedRecipe {
        private final String name;
        private final String description;
        private final String recipeJson;
        
        public GeneratedRecipe(String name, String description, String recipeJson) {
            this.name = name;
            this.description = description;
            this.recipeJson = recipeJson;
        }
        
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getRecipeJson() { return recipeJson; }
    }
}

