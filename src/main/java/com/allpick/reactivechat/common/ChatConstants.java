package com.allpick.reactivechat.common;

/**
 * 채팅 시스템 전역 상수
 */
public final class ChatConstants {

    private ChatConstants() {}

    // WebSocket 토픽 목적지
    public static final String TOPIC_PUBLIC = "/topic/public";
    public static final String TOPIC_USER_STATUS = "/topic/user-status";
}
