package com.recipe.api.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "transformation_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransformationJob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @ManyToOne
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;
    
    @Column(name = "recipe_names", length = 2000)
    private String recipeNames; // Comma-separated recipe names
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private JobStatus status = JobStatus.PENDING;
    
    @Column(name = "output_path")
    private String outputPath; // Path to transformed output
    
    @Column(name = "log_path")
    private String logPath; // Path to transformation logs
    
    @Column(name = "files_transformed")
    private Integer filesTransformed = 0;
    
    @Column(name = "files_failed")
    private Integer filesFailed = 0;
    
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "started_at")
    private LocalDateTime startedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public enum JobStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }
}

