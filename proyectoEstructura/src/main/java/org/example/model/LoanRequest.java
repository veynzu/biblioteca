package org.example.model;

import java.time.LocalDateTime;
import java.io.Serializable;

public class LoanRequest implements Comparable<LoanRequest>, Serializable {
    private static final long serialVersionUID = 1L;

    private User user;
    private Book book;
    private LocalDateTime requestTimestamp;

    public LoanRequest(User user, Book book) {
        this.user = user;
        this.book = book;
        this.requestTimestamp = LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public Book getBook() {
        return book;
    }

    public LocalDateTime getRequestTimestamp() {
        return requestTimestamp;
    }

    @Override
    public int compareTo(LoanRequest other) {
        // Ordena por timestamp, las solicitudes más antiguas tienen mayor prioridad (menor valor)
        return this.requestTimestamp.compareTo(other.requestTimestamp);
    }

    @Override
    public String toString() {
        return "LoanRequest{" +
                "user=" + (user != null ? user.getUsername() : "null") +
                ", book=" + (book != null ? book.getTitulo() : "null") +
                ", requestTimestamp=" + requestTimestamp +
                '}';
    }
} 