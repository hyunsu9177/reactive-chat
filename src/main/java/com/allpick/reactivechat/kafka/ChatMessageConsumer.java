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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "chat-message", groupId = "chat-group")
    public void listen(String messageJson) {
        try {
            ChatMessage message = objectMapper.readValue(messageJson, ChatMessage.class);
            messagingTemplate.convertAndSend("/topic/chatroom", message);
        } catch (Exception e) {
            log.error("Kafka 메시지 처리 실패", e);
        }
    }
}
