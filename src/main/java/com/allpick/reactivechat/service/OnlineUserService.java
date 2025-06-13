package com.allpick.reactivechat.service;

import com.allpick.reactivechat.model.dto.UserStatusDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Slf4j
@Service
@RequiredArgsConstructor
public class OnlineUserService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final Set<String> onlineUsers = new CopyOnWriteArraySet<>();
    
    public void addUser(String userId) {
        if (onlineUsers.add(userId)) {
            log.info("User {} connected. Total online users: {}", userId, onlineUsers.size());
            broadcastUserStatusChange();
        }
    }
    
    public void removeUser(String userId) {
        if (onlineUsers.remove(userId)) {
            log.info("User {} disconnected. Total online users: {}", userId, onlineUsers.size());
            broadcastUserStatusChange();
        }
    }
    
    public Set<String> getOnlineUsers() {
        return Set.copyOf(onlineUsers);
    }
    
    public boolean isUserOnline(String userId) {
        return onlineUsers.contains(userId);
    }
    
    public int getOnlineUserCount() {
        return onlineUsers.size();
    }
    
    private void broadcastUserStatusChange() {
        UserStatusDto statusDto = UserStatusDto.builder()
                .totalOnlineUsers(onlineUsers.size())
                .onlineUsers(Set.copyOf(onlineUsers))
                .build();
        
        messagingTemplate.convertAndSend("/topic/user-status", statusDto);
    }
}