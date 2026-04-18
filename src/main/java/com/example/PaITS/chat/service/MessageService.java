package com.example.PaITS.chat.service;

import com.example.PaITS.chat.dto.MessageRequestDTO;
import com.example.PaITS.chat.dto.MessageResponseDTO;
import com.example.PaITS.user.entity.User;

import java.util.List;
import java.util.UUID;

public interface MessageService {

    MessageResponseDTO sendMessage(UUID projectId, User currentUser, MessageRequestDTO request);

    List<MessageResponseDTO> getProjectMessages(UUID projectId, User currentUser);
}
