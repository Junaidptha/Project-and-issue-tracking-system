package com.example.PaITS.project.service;

import com.example.PaITS.project.dto.ProjectRequestDTO;
import com.example.PaITS.project.dto.ProjectResponseDTO;
import com.example.PaITS.user.entity.User;

import java.util.List;
import java.util.UUID;

public interface ProjectService {

    ProjectResponseDTO saveProject(ProjectRequestDTO request, UUID creatorId);

    List<ProjectResponseDTO> findAll();

    List<ProjectResponseDTO> findAssignedProjects(UUID userId);

    ProjectResponseDTO findById(UUID id, User currentUser);

    ProjectResponseDTO updateProject(UUID id, ProjectRequestDTO details, User currentUser);

    void deleteProject(UUID id, User currentUser);

    ProjectResponseDTO addMember(UUID projectId, UUID userId, User currentUser);

    ProjectResponseDTO removeMember(UUID projectId, UUID userId, User currentUser);

    List<com.example.PaITS.user.dto.PublicUserResponse> getProjectMembers(UUID projectId, User currentUser);
}