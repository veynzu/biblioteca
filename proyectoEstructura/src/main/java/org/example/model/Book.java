package org.example.model;

import org.example.structures.doubleList.DoubleList; // Importar DoubleList
import java.io.Serializable; // Importar Serializable
// import co.edu.uniquindio.estructuradato.proyectofinal.structures.listasSimples.ListaPropia; // Se añadirá después

public class Book implements Comparable <Book>, Serializable { // Implementar Serializable
    private static final long serialVersionUID = 1L; // Buena práctica

    private String id; // Mantener como identificador único
    private String titulo;
    private String autor;
    private String categoria; // Anteriormente genre
    private int anioPublicacion; // Anteriormente year, como int
    private int stockTotal; // Nuevo campo
    private int ejemplaresDisponibles; // Nuevo campo
    private double calificacionPromedio;
    private DoubleList<Rating> ratings; // Usar DoubleList
    private boolean available; // Nuevo campo para disponibilidad

    public Book() {
        this.ratings = new DoubleList<>(); // Inicializar
        this.stockTotal = 0; // Valor por defecto
        this.ejemplaresDisponibles = 0; // Valor por defecto
        this.available = true; // Por defecto, un libro nuevo es disponible
    }

    public Book(String id, String titulo, String autor, String categoria, int anioPublicacion, int stockInicial) {
        this.id = id;
        this.titulo = titulo;
        this.autor = autor;
        this.categoria = categoria;
        this.anioPublicacion = anioPublicacion;
        this.stockTotal = stockInicial;
        this.ejemplaresDisponibles = stockInicial; // Al inicio, todos los ejemplares están disponibles
        this.calificacionPromedio = 0.0; // Inicializar, se calculará después
        this.ratings = new DoubleList<>(); // Inicializar
        this.available = true; // Por defecto, un libro nuevo es disponible
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getAnioPublicacion() {
        return anioPublicacion;
    }

    public void setAnioPublicacion(int anioPublicacion) {
        this.anioPublicacion = anioPublicacion;
    }

    public int getStockTotal() {
        return stockTotal;
    }

    public void setStockTotal(int stockTotal) {
        this.stockTotal = stockTotal;
        // Opcional: Ajustar ejemplaresDisponibles si stockTotal cambia y es menor
        if (this.ejemplaresDisponibles > stockTotal) {
            this.ejemplaresDisponibles = stockTotal;
        }
    }

    public int getEjemplaresDisponibles() {
        return ejemplaresDisponibles;
    }

    public void setEjemplaresDisponibles(int ejemplaresDisponibles) {
        if (ejemplaresDisponibles < 0) {
            this.ejemplaresDisponibles = 0;
        } else if (ejemplaresDisponibles > this.stockTotal) {
            this.ejemplaresDisponibles = this.stockTotal;
        } else {
            this.ejemplaresDisponibles = ejemplaresDisponibles;
        }
    }

    public boolean prestarEjemplar() {
        if (this.ejemplaresDisponibles > 0) {
            this.ejemplaresDisponibles--;
            return true;
        }
        return false; // No hay ejemplares disponibles
    }

    public void devolverEjemplar() {
        if (this.ejemplaresDisponibles < this.stockTotal) {
            this.ejemplaresDisponibles++;
        }
        // Considerar si se debe lanzar una excepción si se intenta devolver más ejemplares de los que hay en stock total.
        // Por ahora, simplemente no se incrementa más allá del stockTotal.
    }
    
    public boolean hayEjemplaresDisponibles() {
        return this.ejemplaresDisponibles > 0;
    }

    public double getCalificacionPromedio() {
        // Este método debería calcularse basado en la lista de ratings
        // Por ahora lo dejo como está, pero necesitará lógica
        return calificacionPromedio;
    }

    public void setCalificacionPromedio(double calificacionPromedio) {
        // Este setter podría ser privado o usarse con cuidado, ya que el promedio
        // debería derivarse de los ratings individuales.
        this.calificacionPromedio = calificacionPromedio;
    }

    public DoubleList<Rating> getRatings() { // Getter
        return ratings;
    }
    
    /**
     * Añade una nueva valoración a este libro y recalcula la calificación promedio.
     * @param rating La valoración a añadir.
     */
    public void addRating(Rating rating) {
        if (rating != null) {
            this.ratings.addLast(rating); // Añadir a la lista
            recalculateCalificacionPromedio();
        }
    }

    /**
     * Recalcula la calificación promedio basada en la lista de ratings.
     */
    public void recalculateCalificacionPromedio() {
        if (ratings.isEmpty()) {
            this.calificacionPromedio = 0.0;
            return;
        }
        double sum = 0;
        // Se necesita una forma de iterar sobre DoubleList. Asumiré que se añadirá un iterador o un método get(index).
        // Por ahora, este cálculo es un placeholder.
        // Si DoubleList ya es iterable (implementa Iterable<T>), se podría hacer:
        // for (Rating r : ratings) { sum += r.getPuntuacion(); }
        // this.calificacionPromedio = sum / ratings.size();
        
        // Como DoubleList tiene get(index) y size():
        for (int i = 0; i < ratings.size(); i++) {
            sum += ratings.get(i).getPuntuacion();
        }
        this.calificacionPromedio = sum / ratings.size();
    }

    // Getter y Setter para 'available'
    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "Book{" +
                "id='" + id + '\'' +
                ", titulo='" + titulo + '\'' +
                ", autor='" + autor + '\'' +
                ", categoria='" + categoria + '\'' +
                ", anioPublicacion=" + anioPublicacion +
                ", stockTotal=" + stockTotal +
                ", ejemplaresDisponibles=" + ejemplaresDisponibles +
                ", calificacionPromedio=" + String.format("%.2f", calificacionPromedio) +
                ", ratingsCount=" + (ratings != null ? ratings.size() : 0) +
                ", available=" + available +
                '}';
    }

    @Override
    public int compareTo(Book other) {
        return this.titulo.compareTo(other.getTitulo());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return id.equals(book.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
