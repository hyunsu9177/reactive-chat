package com.allpick.reactivechat.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.allpick.reactivechat.model.ChatMessage;

@Service
@RequiredArgsConstructor
public class ChatMessageProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(ChatMessage message) {
        kafkaTemplate.send("chat-message", toJson(message));
    }

    private String toJson(ChatMessage msg) {
        // 간단한 직렬화
        return String.format("{\"sender\":\"%s\",\"content\":\"%s\",\"type\":\"%s\"}",
                msg.getSender(), msg.getContent(), msg.getType());
    }
}
