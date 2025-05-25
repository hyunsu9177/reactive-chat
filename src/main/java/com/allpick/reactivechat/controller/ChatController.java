package com.allpick.reactivechat.controller;

import com.allpick.reactivechat.kafka.ChatMessageProducer;
import com.allpick.reactivechat.model.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageProducer producer;

    @MessageMapping("/chat.send")
    public void handleMessage(ChatMessage message) {
        producer.sendMessage(message); // Kafka에 메시지 전송
    }
}
