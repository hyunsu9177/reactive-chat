package com.allpick.reactivechat.kafka;

import com.allpick.reactivechat.model.ChatMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatMessageProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ChatMessageProducer chatMessageProducer;

    @BeforeEach
    void setUp() {
        chatMessageProducer = new ChatMessageProducer(kafkaTemplate, objectMapper);
        ReflectionTestUtils.setField(chatMessageProducer, "chatMessageTopic", "chat-message");
    }

    @Test
    void 메시지_전송_시_KafkaTemplate_send_호출() throws Exception {
        ChatMessage message = ChatMessage.builder()
                .sender("user1")
                .content("hello")
                .type(ChatMessage.MessageType.CHAT)
                .build();

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        when(kafkaTemplate.send(eq("chat-message"), anyString())).thenReturn(future);

        chatMessageProducer.sendMessage(message);

        String expectedJson = objectMapper.writeValueAsString(message);
        verify(kafkaTemplate).send("chat-message", expectedJson);
    }

    @Test
    void 직렬화_실패_시_예외_전파_없음() {
        // ObjectMapper를 mock하여 예외 발생시킴
        ObjectMapper failingMapper = mock(ObjectMapper.class);
        ChatMessageProducer producer = new ChatMessageProducer(kafkaTemplate, failingMapper);
        ReflectionTestUtils.setField(producer, "chatMessageTopic", "chat-message");

        try {
            when(failingMapper.writeValueAsString(any())).thenThrow(new RuntimeException("직렬화 실패"));
        } catch (Exception ignored) {}

        ChatMessage message = ChatMessage.builder()
                .sender("user1")
                .content("hello")
                .build();

        // 예외가 전파되지 않아야 함
        producer.sendMessage(message);

        verifyNoInteractions(kafkaTemplate);
    }
}
