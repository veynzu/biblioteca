package org.example.model;

import java.io.Serializable;

public class Loan implements Serializable {
    private static final long serialVersionUID = 1L;

    private User usuario;
    private Book libro;
    private String fechaSolicitud;
    private String fechaDevolucionPrevista;
    private String fechaDevolucionReal; // Puede ser null si no se ha devuelto
    private boolean devuelto;

    public Loan() {
    }

    public Loan(User usuario, Book libro, String fechaSolicitud, String fechaDevolucionPrevista) {
        this.usuario = usuario;
        this.libro = libro;
        this.fechaSolicitud = fechaSolicitud;
        this.fechaDevolucionPrevista = fechaDevolucionPrevista;
        this.devuelto = false;
        this.fechaDevolucionReal = null; // Inicialmente null
    }

    public User getUsuario() {
        return usuario;
    }

    public void setUsuario(User usuario) {
        this.usuario = usuario;
    }

    public Book getLibro() {
        return libro;
    }

    public void setLibro(Book libro) {
        this.libro = libro;
    }

    public String getFechaSolicitud() {
        return fechaSolicitud;
    }

    public void setFechaSolicitud(String fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }

    public String getFechaDevolucionPrevista() {
        return fechaDevolucionPrevista;
    }

    public void setFechaDevolucionPrevista(String fechaDevolucionPrevista) {
        this.fechaDevolucionPrevista = fechaDevolucionPrevista;
    }

    public String getFechaDevolucionReal() {
        return fechaDevolucionReal;
    }

    public void setFechaDevolucionReal(String fechaDevolucionReal) {
        this.fechaDevolucionReal = fechaDevolucionReal;
    }

    public boolean isDevuelto() { // Cambiado de isReturned
        return devuelto;
    }

    public void setDevuelto(boolean devuelto) { // Cambiado de setReturned
        this.devuelto = devuelto;
    }

    @Override
    public String toString() {
        return "Loan{" +
                "usuario=" + (usuario != null ? usuario.getUsername() : "null") +
                ", libro=" + (libro != null ? libro.getTitulo() : "null") +
                ", fechaSolicitud='" + fechaSolicitud + '\'' +
                ", fechaDevolucionPrevista='" + fechaDevolucionPrevista + '\'' +
                ", fechaDevolucionReal='" + fechaDevolucionReal + '\'' +
                ", devuelto=" + devuelto +
                '}';
    }
}
