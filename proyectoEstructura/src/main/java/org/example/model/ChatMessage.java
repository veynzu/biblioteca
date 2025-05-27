package org.example.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatMessage implements Serializable {
    private static final long serialVersionUID = 1L; // Para la serialización

    private String senderUsername;
    private String roomName;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessage(String senderUsername, String roomName, String content) {
        this.senderUsername = senderUsername;
        this.roomName = roomName;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getSenderUsername() {
        return senderUsername;
    }

    public String getRoomName() {
        return roomName;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    // Opcional: un método para formatear el mensaje para visualización
    public String getFormattedMessage() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return String.format("[%s] %s: %s", timestamp.format(formatter), senderUsername, content);
    }

    @Override
    public String toString() {
        return "ChatMessage{" +
                "senderUsername='" + senderUsername + '\'' +
                ", roomName='" + roomName + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
} 