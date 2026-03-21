package com.allpick.reactivechat.service;

import com.allpick.reactivechat.common.ChatConstants;
import com.allpick.reactivechat.model.dto.UserStatusDto;
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
class OnlineUserServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private OnlineUserService onlineUserService;

    @BeforeEach
    void setUp() {
        onlineUserService = new OnlineUserService(messagingTemplate);
    }

    @Test
    void 사용자_추가_시_온라인_목록에_포함된다() {
        onlineUserService.addUser("user1");

        assertThat(onlineUserService.getOnlineUsers()).contains("user1");
        assertThat(onlineUserService.getOnlineUserCount()).isEqualTo(1);
    }

    @Test
    void 같은_사용자_중복_추가_시_한_번만_추가된다() {
        onlineUserService.addUser("user1");
        onlineUserService.addUser("user1");

        assertThat(onlineUserService.getOnlineUserCount()).isEqualTo(1);
        // 첫 번째 추가 시에만 브로드캐스트
        verify(messagingTemplate, times(1))
                .convertAndSend(eq(ChatConstants.TOPIC_USER_STATUS), any(UserStatusDto.class));
    }

    @Test
    void 사용자_제거_시_온라인_목록에서_제거된다() {
        onlineUserService.addUser("user1");
        onlineUserService.removeUser("user1");

        assertThat(onlineUserService.getOnlineUsers()).doesNotContain("user1");
        assertThat(onlineUserService.getOnlineUserCount()).isEqualTo(0);
    }

    @Test
    void 존재하지_않는_사용자_제거_시_에러_없음() {
        onlineUserService.removeUser("nonexistent");

        assertThat(onlineUserService.getOnlineUserCount()).isEqualTo(0);
        // 브로드캐스트 호출되지 않음
        verify(messagingTemplate, never())
                .convertAndSend(eq(ChatConstants.TOPIC_USER_STATUS), any(UserStatusDto.class));
    }

    @Test
    void 온라인_사용자_수가_정확하다() {
        onlineUserService.addUser("user1");
        onlineUserService.addUser("user2");
        onlineUserService.addUser("user3");

        assertThat(onlineUserService.getOnlineUserCount()).isEqualTo(3);
    }

    @Test
    void 사용자_추가_시_브로드캐스트가_호출된다() {
        onlineUserService.addUser("user1");

        ArgumentCaptor<UserStatusDto> captor = ArgumentCaptor.forClass(UserStatusDto.class);
        verify(messagingTemplate).convertAndSend(eq(ChatConstants.TOPIC_USER_STATUS), captor.capture());

        UserStatusDto dto = captor.getValue();
        assertThat(dto.getTotalOnlineUsers()).isEqualTo(1);
        assertThat(dto.getOnlineUsers()).contains("user1");
    }

    @Test
    void 사용자_제거_시_브로드캐스트가_호출된다() {
        onlineUserService.addUser("user1");
        reset(messagingTemplate);

        onlineUserService.removeUser("user1");

        ArgumentCaptor<UserStatusDto> captor = ArgumentCaptor.forClass(UserStatusDto.class);
        verify(messagingTemplate).convertAndSend(eq(ChatConstants.TOPIC_USER_STATUS), captor.capture());

        UserStatusDto dto = captor.getValue();
        assertThat(dto.getTotalOnlineUsers()).isEqualTo(0);
        assertThat(dto.getOnlineUsers()).isEmpty();
    }
}
