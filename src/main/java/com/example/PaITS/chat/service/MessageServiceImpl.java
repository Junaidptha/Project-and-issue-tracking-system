package com.example.PaITS.chat.service;

import com.example.PaITS.chat.dto.MessageRequestDTO;
import com.example.PaITS.chat.dto.MessageResponseDTO;
import com.example.PaITS.chat.entity.Message;
import com.example.PaITS.chat.repository.MessageRepository;
import com.example.PaITS.project.entity.Project;
import com.example.PaITS.project.repository.ProjectRepository;
import com.example.PaITS.project.service.ProjectService;
import com.example.PaITS.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    @Override
    @Transactional
    public MessageResponseDTO sendMessage(UUID projectId, User currentUser, MessageRequestDTO request) {
        // ProjectService.findById implicitly checks if the currentUser has access to the project
        // (isAdmin || isLeader || isMember). It throws RuntimeException if not authorized.
        projectService.findById(projectId, currentUser);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Project not found"));

        Message message = Message.builder()
                .project(project)
                .sender(currentUser)
                .content(request.getContent())
                .build();

        Message saved = messageRepository.save(message);

        return mapToDTO(saved);
    }

    @Override
    public List<MessageResponseDTO> getProjectMessages(UUID projectId, User currentUser) {
        // Automatically check access
        projectService.findById(projectId, currentUser);

        return messageRepository.findByProjectIdOrderByTimestampAsc(projectId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    private MessageResponseDTO mapToDTO(Message message) {
        return MessageResponseDTO.builder()
                .id(message.getId())
                .projectId(message.getProject().getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getFullName())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();
    }
}
