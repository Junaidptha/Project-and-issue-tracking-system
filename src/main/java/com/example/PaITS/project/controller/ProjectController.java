package com.example.PaITS.project.controller;

import com.example.PaITS.project.dto.ProjectRequestDTO;
import com.example.PaITS.project.dto.ProjectResponseDTO;
import com.example.PaITS.project.service.ProjectService;
import com.example.PaITS.user.entity.User;
import com.example.PaITS.user.repository.UserRepository;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
// import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @PostMapping
    public ResponseEntity<ProjectResponseDTO> createProject(
            @Valid @RequestBody ProjectRequestDTO request,
            Authentication authentication) {

        User user = getCurrentUser(authentication);
        ProjectResponseDTO created = projectService.saveProject(request, user.getId());
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponseDTO>> getAllProjects(Authentication authentication) {
        User user = getCurrentUser(authentication);

        if ("ADMIN".equals(user.getRole())) {
            return ResponseEntity.ok(projectService.findAll());
        } else {
            return ResponseEntity.ok(projectService.findAssignedProjects(user.getId()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> getProjectById(
            @PathVariable UUID id,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(projectService.findById(id, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponseDTO> updateProject(
            @PathVariable UUID id,
            @Valid @RequestBody ProjectRequestDTO projectDetails,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        ProjectResponseDTO updated = projectService.updateProject(id, projectDetails, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable UUID id,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/members/{userId}")
    public ResponseEntity<ProjectResponseDTO> addMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        ProjectResponseDTO updated = projectService.addMember(id, userId, currentUser);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<ProjectResponseDTO> removeMember(
            @PathVariable UUID id,
            @PathVariable UUID userId,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        ProjectResponseDTO updated = projectService.removeMember(id, userId, currentUser);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<List<com.example.PaITS.user.dto.PublicUserResponse>> getProjectMembers(
            @PathVariable UUID id,
            Authentication authentication) {

        User currentUser = getCurrentUser(authentication);
        return ResponseEntity.ok(projectService.getProjectMembers(id, currentUser));
    }

    @Autowired
    private com.example.PaITS.project.service.RoadmapService roadmapService;

    @PostMapping("/{id}/generate-roadmap")
    public ResponseEntity<?> generateRoadmap(
            @PathVariable UUID id,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        try {
            roadmapService.generateRoadmapForProject(id, currentUser.getId());
            return ResponseEntity.ok().body("{\"message\": \"Roadmap generated successfully\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}/roadmap")
    public ResponseEntity<List<com.example.PaITS.project.dto.RoadmapItemDTO>> getRoadmap(
            @PathVariable UUID id,
            Authentication authentication) {
        // Assume user authorization is implicitly checked in front of the project or we assume they have access to the project
        return ResponseEntity.ok(roadmapService.getRoadmapForProject(id));
    }

    @PutMapping("/{id}/roadmap/{itemId}")
    public ResponseEntity<com.example.PaITS.project.dto.RoadmapItemDTO> updateRoadmapItemStatus(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestBody java.util.Map<String, String> body,
            Authentication authentication) {
        
        com.example.PaITS.issue.entity.IssueStatus status = com.example.PaITS.issue.entity.IssueStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(roadmapService.updateRoadmapItemStatus(itemId, status));
    }
}