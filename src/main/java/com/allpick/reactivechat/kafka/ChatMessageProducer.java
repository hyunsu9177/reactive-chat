package com.allpick.reactivechat.kafka;

import com.allpick.reactivechat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendMessage(ChatMessage message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            kafkaTemplate.send("chat-message", jsonMessage);
            log.info("Message sent to Kafka: {}", message);
        } catch (Exception e) {
            log.error("Error sending message to Kafka: {}", message, e);
        }
    }
    
    public void sendJoinMessage(String userId) {
        ChatMessage joinMessage = ChatMessage.builder()
                .sender(userId)
                .content(userId + " joined the chat")
                .type(ChatMessage.MessageType.JOIN)
                .timestamp(String.valueOf(System.currentTimeMillis()))
                .build();
        sendMessage(joinMessage);
    }
    
    public void sendLeaveMessage(String userId) {
        ChatMessage leaveMessage = ChatMessage.builder()
                .sender(userId)
                .content(userId + " left the chat")
                .type(ChatMessage.MessageType.LEAVE)
                .timestamp(String.valueOf(System.currentTimeMillis()))
                .build();
        sendMessage(leaveMessage);
    }
}