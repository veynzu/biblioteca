package org.example.model;


public class Message {
    private User remitente;
    private User destinatario;
    private String contenido;
    private String fechaEnvio;
    private boolean leido;

    public Message() {
    }

    public Message(User remitente, User destinatario, String contenido, String fechaEnvio) {
        this.remitente = remitente;
        this.destinatario = destinatario;
        this.contenido = contenido;
        this.fechaEnvio = fechaEnvio;
        this.leido = false; // Por defecto, un mensaje nuevo no está leído
    }

    public User getRemitente() {
        return remitente;
    }

    public void setRemitente(User remitente) {
        this.remitente = remitente;
    }

    public User getDestinatario() {
        return destinatario;
    }

    public void setDestinatario(User destinatario) {
        this.destinatario = destinatario;
    }

    public String getContenido() {
        return contenido;
    }

    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public String getFechaEnvio() {
        return fechaEnvio;
    }

    public void setFechaEnvio(String fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }

    public boolean isLeido() {
        return leido;
    }

    public void setLeido(boolean leido) {
        this.leido = leido;
    }

    @Override
    public String toString() {
        return "Message{" +
                "remitente=" + (remitente != null ? remitente.getUsername() : "null") +
                ", destinatario=" + (destinatario != null ? destinatario.getUsername() : "null") +
                ", contenido='" + contenido + '\'' +
                ", fechaEnvio='" + fechaEnvio + '\'' +
                ", leido=" + leido +
                '}';
    }
}
