package com.recipe.api.services;

import com.recipe.api.dtos.ProjectDto;
import com.recipe.api.models.Project;
import com.recipe.api.repositories.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    
    private final ProjectRepository projectRepository;
    
    @Autowired
    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }
    
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    public Optional<ProjectDto> getProjectById(Long id) {
        return projectRepository.findById(id)
                .map(this::toDto);
    }
    
    @Transactional
    public ProjectDto createProject(ProjectDto dto) {
        Project project = toEntity(dto);
        Project saved = projectRepository.save(project);
        return toDto(saved);
    }
    
    @Transactional
    public Optional<ProjectDto> updateProject(Long id, ProjectDto dto) {
        return projectRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setDescription(dto.getDescription());
                    existing.setStoragePath(dto.getStoragePath());
                    existing.setSourcePath(dto.getSourcePath());
                    existing.setFileCount(dto.getFileCount());
                    return projectRepository.save(existing);
                })
                .map(this::toDto);
    }
    
    @Transactional
    public boolean deleteProject(Long id) {
        if (projectRepository.existsById(id)) {
            projectRepository.deleteById(id);
            return true;
        }
        return false;
    }
    
    private ProjectDto toDto(Project project) {
        return new ProjectDto(
                project.getId(),
                project.getName(),
                project.getDescription(),
                project.getStoragePath(),
                project.getSourcePath(),
                project.getFileCount(),
                project.getCreatedAt(),
                project.getUpdatedAt()
        );
    }
    
    private Project toEntity(ProjectDto dto) {
        Project project = new Project();
        project.setName(dto.getName());
        project.setDescription(dto.getDescription());
        project.setStoragePath(dto.getStoragePath());
        project.setSourcePath(dto.getSourcePath());
        project.setFileCount(dto.getFileCount());
        return project;
    }
}

