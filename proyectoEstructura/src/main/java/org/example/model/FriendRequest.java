package org.example.model;

import org.example.model.enums.FriendRequestStatus;
import java.io.Serializable;
import java.time.LocalDateTime;

public class FriendRequest implements Serializable {
    private static final long serialVersionUID = 1L; // Para la serialización

    private String senderUsername;
    private String receiverUsername;
    private LocalDateTime requestTimestamp;
    private FriendRequestStatus status;

    public FriendRequest(String senderUsername, String receiverUsername) {
        this.senderUsername = senderUsername;
        this.receiverUsername = receiverUsername;
        this.requestTimestamp = LocalDateTime.now();
        this.status = FriendRequestStatus.PENDING; // Por defecto, una nueva solicitud está pendiente
    }

    // Getters
    public String getSenderUsername() {
        return senderUsername;
    }

    public String getReceiverUsername() {
        return receiverUsername;
    }

    public LocalDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    public FriendRequestStatus getStatus() {
        return status;
    }

    // Setters (principalmente para el estado)
    public void setStatus(FriendRequestStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FriendRequest that = (FriendRequest) o;
        return (senderUsername.equals(that.senderUsername) && receiverUsername.equals(that.receiverUsername)) ||
               (senderUsername.equals(that.receiverUsername) && receiverUsername.equals(that.senderUsername));
    }

    @Override
    public int hashCode() {
        // Hashcode simétrico para que (A,B) tenga el mismo hash que (B,A)
        // Esto es útil si queremos buscar solicitudes sin importar la dirección en un Set, por ejemplo.
        // Para nuestro caso de listas separadas, no es estrictamente necesario, pero es una buena práctica.
        String user1 = senderUsername.compareTo(receiverUsername) < 0 ? senderUsername : receiverUsername;
        String user2 = senderUsername.compareTo(receiverUsername) < 0 ? receiverUsername : senderUsername;
        return 31 * user1.hashCode() + user2.hashCode();
    }

    @Override
    public String toString() {
        return "FriendRequest{" +
                "sender='" + senderUsername + '\'' +
                ", receiver='" + receiverUsername + '\'' +
                ", status=" + status +
                ", timestamp=" + requestTimestamp +
                '}';
    }
} 