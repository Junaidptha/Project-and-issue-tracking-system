package com.example.PaITS.chat.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
public class MessageResponseDTO {
    private UUID id;
    private UUID projectId;
    private UUID senderId;
    private String senderName;
    private String content;
    private LocalDateTime timestamp;
}
