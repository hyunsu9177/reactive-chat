package com.allpick.reactivechat.kafka;

import com.allpick.reactivechat.common.ChatConstants;
import com.allpick.reactivechat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageConsumerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatMessageConsumer chatMessageConsumer;

    @BeforeEach
    void setUp() {
        chatMessageConsumer = new ChatMessageConsumer(messagingTemplate, objectMapper);
    }

    @Test
    void 유효한_JSON_수신_시_클라이언트에_전송() throws Exception {
        ChatMessage message = ChatMessage.builder()
                .sender("user1")
                .content("hello")
                .type(ChatMessage.MessageType.CHAT)
                .build();
        String json = objectMapper.writeValueAsString(message);

        chatMessageConsumer.listen(json);

        ArgumentCaptor<ChatMessage> captor = ArgumentCaptor.forClass(ChatMessage.class);
        verify(messagingTemplate).convertAndSend(eq(ChatConstants.TOPIC_PUBLIC), captor.capture());

        ChatMessage received = captor.getValue();
        assertThat(received.getSender()).isEqualTo("user1");
        assertThat(received.getContent()).isEqualTo("hello");
        assertThat(received.getType()).isEqualTo(ChatMessage.MessageType.CHAT);
    }

    @Test
    void 잘못된_JSON_수신_시_에러_전파_없음() {
        String invalidJson = "{ invalid json }";

        // 예외가 전파되지 않아야 함
        chatMessageConsumer.listen(invalidJson);

        verifyNoInteractions(messagingTemplate);
    }
}
