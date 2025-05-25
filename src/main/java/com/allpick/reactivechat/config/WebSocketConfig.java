package com.allpick.reactivechat.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // SockJS endpoint 설정
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // CORS 허용
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 수신할 prefix
        registry.setApplicationDestinationPrefixes("/app");
        // 클라이언트가 구독할 수 있는 prefix
        registry.enableSimpleBroker("/topic");
    }
}
