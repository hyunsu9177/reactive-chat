package com.allpick.reactivechat.model.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserStatusDto {
    private String userId;
    private boolean isOnline;
    private int totalOnlineUsers;
    private Set<String> onlineUsers;
}