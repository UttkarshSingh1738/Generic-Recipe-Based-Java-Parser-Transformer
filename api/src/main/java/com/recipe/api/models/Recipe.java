package com.recipe.api.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "recipes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    @Column(name = "recipe_json", columnDefinition = "TEXT")
    private String recipeJson;
    
    private String version;
    
    private String author;
    
    @Column(name = "tags", length = 500)
    private String tags; // Comma-separated tags
    
    @Column(name = "category")
    private String category; // e.g., "migration", "modernization", "refactoring"
    
    @Column(name = "is_public")
    private Boolean isPublic = false;
    
    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

