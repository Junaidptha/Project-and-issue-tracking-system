package com.example.PaITS.chat.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageRequestDTO {

    @NotBlank(message = "Message content cannot be blank")
    private String content;

}
