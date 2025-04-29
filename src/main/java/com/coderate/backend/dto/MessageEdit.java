package com.coderate.backend.dto;

import com.coderate.backend.enums.MessageType;

public class MessageEdit implements Comparable<MessageEdit>  {
    private MessageType messageType;
    private String username;
    private String filePath;
    private int lineIndex;
    private int charIndex;
    private Character character;
    private long timestamp;
    private String clientId;

    public MessageEdit(MessageType messageType, String username, String filePath, int lineIndex, int charIndex, Character character, long timestamp, String clientId) {
        this.messageType = messageType;
        this.username = username;
        this.filePath = filePath;
        this.lineIndex = lineIndex;
        this.charIndex = charIndex;
        this.character = character;
        this.timestamp = timestamp;
        this.clientId = clientId;
    }

    public MessageEdit(MessageType messageType, String username, long timestamp, String clientId) {
        this.messageType = messageType;
        this.username = username;
        this.timestamp = timestamp;
        this.clientId = clientId;
    }

    public MessageEdit() {
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public int getLineIndex() {
        return lineIndex;
    }

    public void setLineIndex(int lineIndex) {
        this.lineIndex = lineIndex;
    }

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public int compareTo(MessageEdit other) {
        return Long.compare(this.timestamp, other.timestamp);
    }

    public int getCharIndex() {
        return charIndex;
    }

    public void setCharIndex(int charIndex) {
        this.charIndex = charIndex;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
