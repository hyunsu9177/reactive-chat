package com.allpick.reactivechat.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    @NotBlank
    private String sender;

    @NotBlank
    @Size(max = 500)
    private String content;

    private MessageType type;
    private String timestamp;

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}
