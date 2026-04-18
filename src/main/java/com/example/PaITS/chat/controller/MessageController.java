package com.example.PaITS.chat.controller;

import com.example.PaITS.chat.dto.MessageRequestDTO;
import com.example.PaITS.chat.dto.MessageResponseDTO;
import com.example.PaITS.chat.service.MessageService;
import com.example.PaITS.user.entity.User;
import com.example.PaITS.user.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping
    public ResponseEntity<List<MessageResponseDTO>> getMessages(
            @PathVariable UUID projectId,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        List<MessageResponseDTO> messages = messageService.getProjectMessages(projectId, currentUser);
        return ResponseEntity.ok(messages);
    }

    @PostMapping
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @PathVariable UUID projectId,
            @Valid @RequestBody MessageRequestDTO request,
            Authentication authentication) {
        
        User currentUser = getCurrentUser(authentication);
        MessageResponseDTO savedMessage = messageService.sendMessage(projectId, currentUser, request);
        return new ResponseEntity<>(savedMessage, HttpStatus.CREATED);
    }
}
