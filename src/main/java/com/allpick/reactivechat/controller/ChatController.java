package com.allpick.reactivechat.controller;

import com.allpick.reactivechat.kafka.ChatMessageProducer;
import com.allpick.reactivechat.model.ChatMessage;
import com.allpick.reactivechat.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ChatController {

    private final OnlineUserService onlineUserService;
    private final ChatMessageProducer chatMessageProducer;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessage chatMessage) {
        log.info("Received message: {}", chatMessage);
        chatMessage.setTimestamp(LocalDateTime.now().toString());
        chatMessageProducer.sendMessage(chatMessage);
    }

    @MessageMapping("/chat.addUser")
    public void addUser(@Payload ChatMessage chatMessage, 
                       SimpMessageHeaderAccessor headerAccessor) {
        log.info("User joining: {}", chatMessage.getSender());
        // 세션에 사용자명 저장
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        
        // 사용자를 온라인 목록에 추가
        onlineUserService.addUser(chatMessage.getSender());
        
        // JOIN 메시지를 Kafka로 전송
        chatMessage.setTimestamp(LocalDateTime.now().toString());
        chatMessage.setType(ChatMessage.MessageType.JOIN);
        chatMessageProducer.sendMessage(chatMessage);
    }

    @GetMapping("/api/online-users")
    public Set<String> getOnlineUsers() {
        return onlineUserService.getOnlineUsers();
    }

    @GetMapping("/api/online-users/count")
    public int getOnlineUserCount() {
        return onlineUserService.getOnlineUserCount();
    }
}