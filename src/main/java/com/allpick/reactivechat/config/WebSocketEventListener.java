package com.allpick.reactivechat.config;

import com.allpick.reactivechat.kafka.ChatMessageProducer;
import com.allpick.reactivechat.model.ChatMessage;
import com.allpick.reactivechat.service.OnlineUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final OnlineUserService onlineUserService;
    private final ChatMessageProducer chatMessageProducer;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        
        if (username != null) {
            log.info("User {} disconnected", username);
            
            // 사용자를 온라인 목록에서 제거
            onlineUserService.removeUser(username);
            
            // LEAVE 메시지를 Kafka로 전송
            ChatMessage leaveMessage = ChatMessage.builder()
                    .sender(username)
                    .content(username + "님이 채팅을 떠났습니다.")
                    .type(ChatMessage.MessageType.LEAVE)
                    .timestamp(LocalDateTime.now().toString())
                    .build();
            
            chatMessageProducer.sendMessage(leaveMessage);
        }
    }
}