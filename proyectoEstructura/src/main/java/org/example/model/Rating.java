package org.example.model;

import java.io.Serializable; // Importar Serializable

public class Rating implements Serializable { // Implementar Serializable
    private static final long serialVersionUID = 1L; // Buena práctica

    private User usuario; // Cambiado de user
    private Book libro;   // Cambiado de book
    private int puntuacion; // Cambiado de rating (1-5)
    private String comentario; // Cambiado de comment
    private String fechaValoracion; // Nuevo atributo, puede ser String o Date

    public Rating() {
    }

    public Rating(User usuario, Book libro, int puntuacion, String comentario, String fechaValoracion) {
        this.usuario = usuario;
        this.libro = libro;
        // Validar puntuación (1-5) aquí o en un setter si es necesario
        this.puntuacion = puntuacion;
        this.comentario = comentario;
        this.fechaValoracion = fechaValoracion;
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

    public int getPuntuacion() {
        return puntuacion;
    }

    public void setPuntuacion(int puntuacion) {
        // Considerar validación 1-5
        this.puntuacion = puntuacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getFechaValoracion() {
        return fechaValoracion;
    }

    public void setFechaValoracion(String fechaValoracion) {
        this.fechaValoracion = fechaValoracion;
    }

    @Override
    public String toString() {
        return "Rating{" +
                "usuario=" + (usuario != null ? usuario.getUsername() : "null") + // Evitar NullPointerException si usuario es null y mostrar username
                ", libro=" + (libro != null ? libro.getTitulo() : "null") + // Evitar NullPointerException y mostrar titulo
                ", puntuacion=" + puntuacion +
                ", comentario='" + comentario + '\'' +
                ", fechaValoracion='" + fechaValoracion + '\'' +
                '}';
    }
}
