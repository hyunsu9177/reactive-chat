package com.allpick.reactivechat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String sender;
    private String content;
    private MessageType type;
    private String timestamp;
    
    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}