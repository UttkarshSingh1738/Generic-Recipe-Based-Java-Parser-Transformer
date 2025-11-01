package com.recipe.api.repositories;

import com.recipe.api.models.TransformationJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<TransformationJob, Long> {
    List<TransformationJob> findByProjectId(Long projectId);
    
    List<TransformationJob> findByStatus(TransformationJob.JobStatus status);
    
    @Query("SELECT j FROM TransformationJob j WHERE j.project.id = :projectId ORDER BY j.createdAt DESC")
    List<TransformationJob> findRecentJobsByProject(@Param("projectId") Long projectId, org.springframework.data.domain.Pageable pageable);
}

