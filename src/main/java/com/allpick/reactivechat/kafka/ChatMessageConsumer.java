package com.allpick.reactivechat.kafka;

import com.allpick.reactivechat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "chat-message", groupId = "chat-group")
    public void listen(String message) {
        try {
            ChatMessage chatMessage = objectMapper.readValue(message, ChatMessage.class);
            log.info("Received message from Kafka: {}", chatMessage);
            
            // 모든 연결된 클라이언트에게 메시지 전송
            messagingTemplate.convertAndSend("/topic/public", chatMessage);
            
        } catch (Exception e) {
            log.error("Error processing message: {}", message, e);
        }
    }
}