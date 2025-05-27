package org.example;

import org.example.model.Book;
import org.example.model.User;
// import org.example.model.enums.EstadoLibro; // ELIMINADO: Ya no se necesita
import org.example.model.enums.TipoUsuario;
// Ya no necesitamos importar Loan ni DoubleList directamente en Main si no los usamos explícitamente aquí.

import javax.swing.SwingUtilities; // Para invocar en EDT
import org.example.structures.doubleList.DoubleList;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import org.example.DigitalLibraryView; // Importar la vista

// La vista se comentará hasta que se implemente
// import org.example.DigitalLibraryView;

public class Main {
    private static final String DATA_FILE_PATH = "library_data.dat";
    private static DigitalLibrary biblioteca;
    private static final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static void main(String[] args) {
        biblioteca = DigitalLibrary.loadData(DATA_FILE_PATH);
        if (biblioteca == null) {
            System.out.println("No se encontraron datos guardados o hubo un error al cargar. Creando nueva biblioteca...");
            biblioteca = new DigitalLibrary();
            initializeSampleData();
            biblioteca.saveData(DATA_FILE_PATH); // Guardar inmediatamente después de inicializar por primera vez
        }

        // Lanzar la Interfaz Gráfica
        final DigitalLibrary finalBiblioteca = biblioteca; // Necesario para la clase anónima
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // Asumiendo que DigitalLibraryView tiene un constructor que acepta DigitalLibrary
                DigitalLibraryView view = new DigitalLibraryView(finalBiblioteca);
                view.setVisible(true);
                // Aquí se podría añadir un WindowListener a 'view' para guardar al cerrar,
                // o manejarlo dentro de DigitalLibraryView.
            }
        });
    }

    private static void initializeSampleData() {
        System.out.println("Inicializando datos de ejemplo para la biblioteca...");
        
        // Libros de ejemplo (ajustando stock según solicitud)
        biblioteca.addBook(new Book("B001", "El Señor de los Anillos", "J.R.R. Tolkien", "Fantasía", 1954, 5));
        biblioteca.addBook(new Book("B002", "Cien Años de Soledad", "Gabriel García Márquez", "Realismo Mágico", 1967, 1)); // Stock ajustado a 1
        biblioteca.addBook(new Book("B003", "1984", "George Orwell", "Distopía", 1949, 0)); // Stock 0 para pruebas de lista de espera
        biblioteca.addBook(new Book("B004", "Don Quijote de la Mancha", "Miguel de Cervantes", "Clásico", 1605, 1)); // Stock ajustado a 1
        biblioteca.addBook(new Book("B005", "Harry Potter y la Piedra Filosofal", "J.K. Rowling", "Fantasía", 1997, 10));
        biblioteca.addBook(new Book("B006", "Orgullo y Prejuicio", "Jane Austen", "Romance Clásico", 1813, 4));
        biblioteca.addBook(new Book("B007", "Crónica de una Muerte Anunciada", "Gabriel García Márquez", "Novela Corta", 1981, 1)); // Ya tenía stock 1
        biblioteca.addBook(new Book("B008", "El Código Da Vinci", "Dan Brown", "Misterio", 2003, 0)); // Stock 0
        biblioteca.addBook(new Book("B009", "Matar un Ruiseñor", "Harper Lee", "Ficción Legal", 1960, 3));
        biblioteca.addBook(new Book("B010", "La Sombra del Viento", "Carlos Ruiz Zafón", "Misterio", 2001, 6));
        // Añadir dos libros más con stock 1 para asegurar variedad si se cambian los anteriores
        biblioteca.addBook(new Book("B011", "Fahrenheit 451", "Ray Bradbury", "Ciencia Ficción", 1953, 1));
        biblioteca.addBook(new Book("B012", "El Gran Gatsby", "F. Scott Fitzgerald", "Clásico Moderno", 1925, 1));

        // Usuarios con credenciales actualizadas
        biblioteca.registerUser(new User("U001", "Ana", "Pérez", "ana@example.com", "user1", "123", TipoUsuario.LECTOR));
        biblioteca.registerUser(new User("U002", "Luis", "Gómez", "luis@example.com", "user2", "123", TipoUsuario.LECTOR));
        biblioteca.registerUser(new User("U003", "Admin", "Principal", "admin@example.com", "admin", "123", TipoUsuario.ADMINISTRADOR));
        biblioteca.registerUser(new User("U004", "Carlos", "Ruiz", "carlos@example.com", "user3", "123", TipoUsuario.LECTOR));
        biblioteca.registerUser(new User("U005", "Elena", "Diaz", "elena@example.com", "elenad", "pass101", TipoUsuario.LECTOR));
        System.out.println("Datos de ejemplo inicializados en la biblioteca con credenciales actualizadas.");
    }

    private static void verCatalogo() {
        System.out.println("\n--- Catálogo de Libros ---");
        DoubleList<Book> catalogo = biblioteca.getCatalogoLibros().inOrderTraversal();
        if (catalogo.isEmpty()) {
            System.out.println("El catálogo está vacío.");
            return;
        }
        for (int i = 0; i < catalogo.size(); i++) {
            Book libro = catalogo.get(i);
            System.out.println(String.format("ID: %s, Título: %s, Autor: %s, Categoría: %s, Año: %d, Stock: %d, Disp: %d, Rating: %.2f", 
                libro.getId(), libro.getTitulo(), libro.getAutor(), libro.getCategoria(), 
                libro.getAnioPublicacion(), libro.getStockTotal(), libro.getEjemplaresDisponibles(), libro.getCalificacionPromedio()));
        }
    }
    
    // Aquí puedes añadir más métodos para interactuar con la biblioteca desde la consola
    // como los que tenías antes o nuevos para probar la persistencia.
}