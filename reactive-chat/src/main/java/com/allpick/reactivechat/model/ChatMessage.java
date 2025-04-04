package com.allpick.reactivechat.model;

public class ChatMessage {

    private String sender;
    private String content;
    private String type; // 예: CHAT, JOIN, LEAVE 등

    public ChatMessage() {}

    public ChatMessage(String sender, String content, String type) {
        this.sender = sender;
        this.content = content;
        this.type = type;
    }

    // Getters & Setters
    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
