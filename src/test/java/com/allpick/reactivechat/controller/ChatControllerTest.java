package com.allpick.reactivechat.controller;

import com.allpick.reactivechat.kafka.ChatMessageProducer;
import com.allpick.reactivechat.model.ChatMessage;
import com.allpick.reactivechat.service.OnlineUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private OnlineUserService onlineUserService;

    @Mock
    private ChatMessageProducer chatMessageProducer;

    private Clock fixedClock;
    private ChatController chatController;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2026-03-21T12:00:00Z"), ZoneId.of("UTC"));
        chatController = new ChatController(onlineUserService, chatMessageProducer, fixedClock);
    }

    @Test
    void sendMessage_타임스탬프_설정_및_Kafka_전송() {
        ChatMessage message = ChatMessage.builder()
                .sender("user1")
                .content("hello")
                .type(ChatMessage.MessageType.CHAT)
                .build();

        chatController.sendMessage(message);

        assertThat(message.getTimestamp()).isNotNull();
        assertThat(message.getId()).isNotNull();
        verify(chatMessageProducer).sendMessage(message);
    }

    @Test
    void addUser_세션_저장_및_온라인_추가_및_Kafka_전송() {
        ChatMessage message = ChatMessage.builder()
                .sender("user1")
                .content("user1님이 채팅에 참여했습니다.")
                .build();

        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        HashMap<String, Object> sessionAttrs = new HashMap<>();
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttrs);

        chatController.addUser(message, headerAccessor);

        // 세션에 username 저장 확인
        assertThat(sessionAttrs.get("username")).isEqualTo("user1");
        // 온라인 목록에 추가 확인
        verify(onlineUserService).addUser("user1");
        // Kafka 전송 확인
        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(chatMessageProducer).sendMessage(captor.capture());
        ChatMessage sent = captor.getValue();
        assertThat(sent.getType()).isEqualTo(ChatMessage.MessageType.JOIN);
        assertThat(sent.getTimestamp()).isNotNull();
        assertThat(sent.getId()).isNotNull();
    }

    @Test
    void getOnlineUsers_목록_반환() {
        when(onlineUserService.getOnlineUsers()).thenReturn(Set.of("user1", "user2"));

        Set<String> result = chatController.getOnlineUsers();

        assertThat(result).containsExactlyInAnyOrder("user1", "user2");
    }

    @Test
    void getOnlineUserCount_수_반환() {
        when(onlineUserService.getOnlineUserCount()).thenReturn(3);

        int count = chatController.getOnlineUserCount();

        assertThat(count).isEqualTo(3);
    }
}
