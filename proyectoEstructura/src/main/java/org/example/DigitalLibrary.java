package org.example;

import org.example.model.Book;
import org.example.model.Loan;
import org.example.model.LoanRequest;
import org.example.model.User;
import org.example.model.Rating;
import org.example.model.enums.TipoUsuario;
import org.example.structures.BinaryTree.BinaryTree;
import org.example.structures.RedBlackTree.RedBlackTree;
import org.example.structures.colaPrioridad.ColaPrioridad;
import org.example.structures.colas.Cola;
import org.example.structures.doubleList.DoubleList;
import org.example.structures.graph.Graph;
import org.example.structures.pilas.Pila;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DigitalLibrary implements Serializable {
    private static final long serialVersionUID = 1L;

    private RedBlackTree<Book> catalogoLibros;
    private DoubleList<User> usuarios;
    private DoubleList<Loan> prestamosActivos;
    private ColaPrioridad<LoanRequest> waitlist;
    private Graph<User> redAfinidad;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DigitalLibrary() {
        this.catalogoLibros = new RedBlackTree<>();
        this.usuarios = new DoubleList<>();
        this.prestamosActivos = new DoubleList<>();
        this.waitlist = new ColaPrioridad<>();
        this.redAfinidad = new Graph<>();
    }

    // --- Métodos de Persistencia ---
    public void saveData(String filePath) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(this);
            System.out.println("Datos de la biblioteca guardados en: " + filePath);
        } catch (IOException e) {
            System.err.println("Error al guardar los datos de la biblioteca: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static DigitalLibrary loadData(String filePath) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath))) {
            DigitalLibrary library = (DigitalLibrary) ois.readObject();
            System.out.println("Datos de la biblioteca cargados desde: " + filePath);
            return library;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar los datos de la biblioteca: " + e.getMessage());
            return null;
        }
    }

    // --- Gestión de Libros ---
    public void addBook(Book book) {
        if (book != null) {
            catalogoLibros.insert(book);
        }
    }

    public Book findBookByTitle(String title) {
        DoubleList<Book> todosLosLibros = catalogoLibros.inOrderTraversal();
        for (int i = 0; i < todosLosLibros.size(); i++) {
            Book libro = todosLosLibros.get(i);
            if (libro.getTitulo().equalsIgnoreCase(title)) {
                return libro;
            }
        }
        return null;
    }
    
    public Book findBookById(String bookId) {
        DoubleList<Book> todosLosLibros = catalogoLibros.inOrderTraversal();
        for (int i = 0; i < todosLosLibros.size(); i++) {
            Book libro = todosLosLibros.get(i);
            if (libro.getId().equals(bookId)) {
                return libro;
            }
        }
        return null;
    }

    // --- Gestión de Usuarios ---
    public void registerUser(User user) {
        if (user != null && findUserByUsername(user.getUsername()) == null) {
            usuarios.addLast(user);
            redAfinidad.addVertex(user);
        }
    }

    public User findUserByUsername(String username) {
        for (int i = 0; i < usuarios.size(); i++) {
            User u = usuarios.get(i);
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }
    
    public User login(String username, String password) {
        User user = findUserByUsername(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    // --- Gestión de Préstamos ---
    public synchronized String requestLoan(String username, String bookId) {
        User user = findUserByUsername(username);
        Book book = findBookById(bookId);

        if (user == null) {
            return "Usuario no encontrado.";
        }
        if (book == null) {
            return "Libro no encontrado.";
        }

        for (int i = 0; i < prestamosActivos.size(); i++) {
            Loan activeLoan = prestamosActivos.get(i);
            if (activeLoan.getUsuario().equals(user) && activeLoan.getLibro().equals(book)) {
                return "Ya tienes este libro prestado.";
            }
        }
        
        if (book.hayEjemplaresDisponibles()) {
            book.prestarEjemplar(); 
            LocalDate fechaSolicitud = LocalDate.now();
            LocalDate fechaDevolucionPrevista = fechaSolicitud.plusWeeks(2);
            Loan newLoan = new Loan(user, book, fechaSolicitud.format(DATE_FORMATTER), fechaDevolucionPrevista.format(DATE_FORMATTER));
            prestamosActivos.addLast(newLoan);
            return "Préstamo exitoso para el libro: " + book.getTitulo();
        } else {
            LoanRequest request = new LoanRequest(user, book);
            waitlist.add(request);
            
            // Buscar la fecha de devolución más próxima para este libro
            String proximaDevolucionStr = "No hay información de próxima devolución.";
            LocalDate proximaDevolucion = null;

            for (int i = 0; i < prestamosActivos.size(); i++) {
                Loan currentLoan = prestamosActivos.get(i);
                if (currentLoan.getLibro().equals(book)) {
                    try {
                        LocalDate fechaDevPrev = LocalDate.parse(currentLoan.getFechaDevolucionPrevista(), DATE_FORMATTER);
                        if (proximaDevolucion == null || fechaDevPrev.isBefore(proximaDevolucion)) {
                            proximaDevolucion = fechaDevPrev;
                        }
                    } catch (Exception e) {
                        // Ignorar si la fecha no se puede parsear, aunque no debería pasar
                        System.err.println("Error parseando fecha de devolución prevista: " + currentLoan.getFechaDevolucionPrevista());
                    }
                }
            }

            if (proximaDevolucion != null) {
                proximaDevolucionStr = "Se espera un ejemplar para el: " + proximaDevolucion.format(DATE_FORMATTER) + ".";
            }

            return "Libro '" + book.getTitulo() + "' no disponible. Añadido a lista de espera. " + proximaDevolucionStr;
        }
    }

    public synchronized String returnLoan(String username, String bookId) {
        User user = findUserByUsername(username);
        Book book = findBookById(bookId);

        if (user == null) {
            return "Usuario no encontrado.";
        }
        if (book == null) {
            return "Libro no encontrado.";
        }

        Loan loanToRemove = null;
        int loanIndex = -1; 
        for (int i = 0; i < prestamosActivos.size(); i++) {
            Loan currentLoan = prestamosActivos.get(i);
            if (currentLoan.getUsuario().equals(user) && currentLoan.getLibro().equals(book) && !currentLoan.isDevuelto()) { 
                loanToRemove = currentLoan;
                loanIndex = i;
                break;
            }
        }

        if (loanToRemove != null) {
            if (loanIndex != -1) {
                 prestamosActivos.removeAtIndex(loanIndex); 
            } else {
                return "Error interno: No se pudo determinar el índice del préstamo a remover.";
            }
            
            book.devolverEjemplar(); 
            updateAffinityNetwork(user, book);
            processWaitlistForBook(book);
            return "Libro '" + book.getTitulo() + "' devuelto exitosamente.";
        } else {
            return "No se encontró un préstamo activo para este usuario y libro.";
        }
    }

    public synchronized String valorarLibro(String username, String bookId, int puntuacion, String comentario) {
        User user = findUserByUsername(username);
        Book book = findBookById(bookId);

        if (user == null) return "Usuario no encontrado.";
        if (book == null) return "Libro no encontrado.";
        if (puntuacion < 1 || puntuacion > 5) return "La puntuación debe estar entre 1 y 5.";

        for(int i=0; i < prestamosActivos.size(); i++){
            Loan l = prestamosActivos.get(i);
            if(l.getUsuario().equals(user) && l.getLibro().equals(book)){
                return "Debes devolver el libro antes de poder valorarlo.";
            }
        }

        Rating newRating = new Rating(user, book, puntuacion, comentario, LocalDateTime.now().format(DATETIME_FORMATTER));
        book.addRating(newRating); 
        user.getLibrosValorados().addLast(newRating); 
        
        return "Libro '" + book.getTitulo() + "' valorado con " + puntuacion + " estrellas por " + username + ".";
    }

    public synchronized void processWaitlistForBook(Book book) {
        if (book == null) return;

        while (book.hayEjemplaresDisponibles() && !waitlist.isEmpty()) { 
            LoanRequest nextRequest = waitlist.peek(); 

            if (nextRequest != null && nextRequest.getBook().equals(book)) {
                waitlist.poll(); 

                book.prestarEjemplar(); 
                LocalDate fechaSolicitud = LocalDate.now();
                LocalDate fechaDevolucionPrevista = fechaSolicitud.plusWeeks(2);
                Loan newLoan = new Loan(nextRequest.getUser(), nextRequest.getBook(), fechaSolicitud.format(DATE_FORMATTER), fechaDevolucionPrevista.format(DATE_FORMATTER));
                prestamosActivos.addLast(newLoan);

            } else if (nextRequest != null && !nextRequest.getBook().equals(book)) {
                break; 
            } else { 
                break;
            }
        }
    }

    // --- Red de Afinidad ---
    public void updateAffinityNetwork(User user1, Book commonBook) {
        for (int i = 0; i < usuarios.size(); i++) {
            User user2 = usuarios.get(i);
            if (!user1.equals(user2)) {
                DoubleList<Loan> historialUser2 = user2.getHistorialPrestamos();
                if (historialUser2 != null) {
                    for (int j = 0; j < historialUser2.size(); j++) {
                        Loan prestamoUser2 = historialUser2.get(j);
                        if (prestamoUser2.getLibro().equals(commonBook)) {
                            redAfinidad.addEdge(user1, user2);
                            break;
                        }
                    }
                }
            }
        }
    }

    public DoubleList<User> getFriendSuggestions(String username) {
        User user = findUserByUsername(username);
        if (user == null) {
            return new DoubleList<>();
        }
        DoubleList<User> neighborsList = redAfinidad.getNeighbors(user);
        
        return neighborsList;
    }

    public DoubleList<User> findShortestPathAffinity(String username1, String username2) {
        User startUser = findUserByUsername(username1);
        User endUser = findUserByUsername(username2);

        if (startUser == null || endUser == null) {
            return new DoubleList<>();
        }

        Cola<User> queue = new Cola<>();
        Pila<User> pathStack = new Pila<>();
        DoubleList<User> visited = new DoubleList<>();
        Map<User, User> predecessors = new HashMap<>();

        queue.enqueue(startUser);
        visited.addLast(startUser);
        predecessors.put(startUser, null);

        boolean found = false;
        while (!queue.isEmpty()) {
            User currentUser = queue.dequeue();
            if (currentUser.equals(endUser)) {
                found = true;
                break;
            }

            DoubleList<User> neighbors = redAfinidad.getNeighbors(currentUser);
            if (neighbors != null && !neighbors.isEmpty()) {
                for (int i = 0; i < neighbors.size(); i++) {
                    User neighbor = neighbors.get(i);
                    boolean alreadyVisited = false;
                    for(int j=0; j < visited.size(); j++){
                        if(visited.get(j).equals(neighbor)){
                            alreadyVisited = true;
                            break;
                        }
                    }
                    if (!alreadyVisited) {
                        visited.addLast(neighbor);
                        queue.enqueue(neighbor);
                        predecessors.put(neighbor, currentUser);
                    }
                }
            }
        }

        DoubleList<User> shortestPath = new DoubleList<>();
        if (found) {
            User step = endUser;
            while (step != null) {
                pathStack.push(step);
                step = predecessors.get(step);
            }
            while(!pathStack.isEmpty()){
                shortestPath.addLast(pathStack.pop());
            }
        }
        return shortestPath;
    }

    // --- Sistema de Recomendaciones ---
    public DoubleList<Book> getBookRecommendations(String username) {
        User user = findUserByUsername(username);
        if (user == null) {
            return new DoubleList<>(); // Usuario no encontrado, devolver lista vacía
        }

        DoubleList<Book> recommendations = new DoubleList<>();
        DoubleList<Rating> userRatings = user.getLibrosValorados();
        
        if (userRatings.isEmpty()) {
            // Si el usuario no ha valorado nada, podríamos devolver los más populares o aleatorios.
            // Por ahora, devolvemos una lista vacía o los N libros mejor valorados en general.
            // Vamos a devolver los N libros con mejor calificación promedio general que el usuario no haya valorado.
            return getTopRatedBooksNotRatedByUser(user, 5); // Recomendar 5 libros
        }

        // Coleccionar categorías y autores de libros bien valorados por el usuario (puntuación >= 4)
        DoubleList<String> favoriteCategories = new DoubleList<>();
        DoubleList<String> favoriteAuthors = new DoubleList<>();
        DoubleList<Book> highlyRatedBooksByUser = new DoubleList<>(); // Libros que el usuario valoró alto

        for (int i = 0; i < userRatings.size(); i++) {
            Rating rating = userRatings.get(i);
            if (rating.getPuntuacion() >= 4) { // Considerar "bien valorado"
                Book ratedBook = rating.getLibro();
                highlyRatedBooksByUser.addLast(ratedBook);
                // Añadir categoría si no está ya (evitar duplicados simples)
                boolean catExists = false;
                for(int j=0; j<favoriteCategories.size(); j++){
                    if(favoriteCategories.get(j).equalsIgnoreCase(ratedBook.getCategoria())){
                        catExists = true; break;
                    }
                }
                if(!catExists) favoriteCategories.addLast(ratedBook.getCategoria());

                // Añadir autor si no está ya
                boolean authorExists = false;
                for(int j=0; j<favoriteAuthors.size(); j++){
                    if(favoriteAuthors.get(j).equalsIgnoreCase(ratedBook.getAutor())){
                        authorExists = true; break;
                    }
                }
                if(!authorExists) favoriteAuthors.addLast(ratedBook.getAutor());
            }
        }

        // Si no hay libros bien valorados, recurrir a los top generales.
        if (favoriteCategories.isEmpty() && favoriteAuthors.isEmpty()) {
            return getTopRatedBooksNotRatedByUser(user, 5);
        }

        // Buscar libros para recomendar
        DoubleList<Book> allBooks = catalogoLibros.inOrderTraversal();
        for (int i = 0; i < allBooks.size(); i++) {
            Book potentialRecommendation = allBooks.get(i);
            boolean alreadyRatedByUser = false;
            for (int j = 0; j < userRatings.size(); j++) {
                if (userRatings.get(j).getLibro().equals(potentialRecommendation)) {
                    alreadyRatedByUser = true;
                    break;
                }
            }
            // También verificar si ya está en la lista de recomendaciones (evitar añadirlo dos veces)
            boolean alreadyRecommended = false;
            for(int k=0; k < recommendations.size(); k++){
                if(recommendations.get(k).equals(potentialRecommendation)){
                    alreadyRecommended = true; break;
                }
            }

            if (!alreadyRatedByUser && !alreadyRecommended) {
                boolean matchesCategory = false;
                for(int j=0; j<favoriteCategories.size(); j++){
                    if(potentialRecommendation.getCategoria().equalsIgnoreCase(favoriteCategories.get(j))){
                        matchesCategory = true; break;
                    }
                }

                boolean matchesAuthor = false;
                 for(int j=0; j<favoriteAuthors.size(); j++){
                    if(potentialRecommendation.getAutor().equalsIgnoreCase(favoriteAuthors.get(j))){
                        matchesAuthor = true; break;
                    }
                }

                if (matchesCategory || matchesAuthor) {
                    // Podríamos añadir una ponderación o simplemente añadirlo.
                    // También podríamos ordenar por calificación promedio general.
                    recommendations.addLast(potentialRecommendation);
                }
            }
        }
        
        // Opcional: Ordenar las recomendaciones por calificación promedio general (descendente)
        // Esta es una ordenación por burbuja simple, se podría usar algo más eficiente
        // si DoubleList tuviera más funcionalidades o si se convierte a una lista de Java temporalmente.
        if (!recommendations.isEmpty()) {
            for (int i = 0; i < recommendations.size() - 1; i++) {
                for (int j = 0; j < recommendations.size() - i - 1; j++) {
                    if (recommendations.get(j).getCalificacionPromedio() < recommendations.get(j + 1).getCalificacionPromedio()) {
                        // Intercambiar
                        Book temp = recommendations.get(j);
                        recommendations.set(j, recommendations.get(j + 1)); // Asume que DoubleList tiene set(index, value)
                        recommendations.set(j + 1, temp);
                    }
                }
            }
        }
        
        // Limitar el número de recomendaciones (e.g., a 5 o 10)
        DoubleList<Book> finalRecommendations = new DoubleList<>();
        for(int i=0; i < recommendations.size() && i < 5; i++){ // Devolver hasta 5 recomendaciones
            finalRecommendations.addLast(recommendations.get(i));
        }

        // Si después de todo esto no hay recomendaciones personalizadas, ofrecer los top generales
        if (finalRecommendations.isEmpty()){
            return getTopRatedBooksNotRatedByUser(user, 5);
        }

        return finalRecommendations;
    }

    // Método auxiliar para obtener los N libros mejor valorados que el usuario no ha valorado
    private DoubleList<Book> getTopRatedBooksNotRatedByUser(User user, int count) {
        DoubleList<Book> allBooks = catalogoLibros.inOrderTraversal();
        DoubleList<Book> ratedByUser = new DoubleList<>();
        for(int i=0; i < user.getLibrosValorados().size(); i++){
            ratedByUser.addLast(user.getLibrosValorados().get(i).getLibro());
        }

        // Filtrar los que no ha valorado
        DoubleList<Book> notRatedBooks = new DoubleList<>();
        for(int i=0; i < allBooks.size(); i++){
            Book currentBook = allBooks.get(i);
            boolean hasRated = false;
            for(int j=0; j < ratedByUser.size(); j++){
                if(currentBook.equals(ratedByUser.get(j))){
                    hasRated = true;
                    break;
                }
            }
            if(!hasRated) {
                notRatedBooks.addLast(currentBook);
            }
        }

        // Ordenar `notRatedBooks` por calificación promedio (descendente)
        // (Usando burbuja simple, asumir que DoubleList tiene set(index, value))
        if (!notRatedBooks.isEmpty()) {
            for (int i = 0; i < notRatedBooks.size() - 1; i++) {
                for (int j = 0; j < notRatedBooks.size() - i - 1; j++) {
                    if (notRatedBooks.get(j).getCalificacionPromedio() < notRatedBooks.get(j + 1).getCalificacionPromedio()) {
                        Book temp = notRatedBooks.get(j);
                        notRatedBooks.set(j, notRatedBooks.get(j + 1));
                        notRatedBooks.set(j + 1, temp);
                    }
                }
            }
        }
        
        DoubleList<Book> topBooks = new DoubleList<>();
        for(int i=0; i < notRatedBooks.size() && i < count; i++){
            topBooks.addLast(notRatedBooks.get(i));
        }
        return topBooks;
    }

    // Getters para las colecciones internas (útil para la GUI o pruebas)
    public RedBlackTree<Book> getCatalogoLibros() {
        return catalogoLibros;
    }

    public DoubleList<User> getUsuarios() {
        return usuarios;
    }

    public DoubleList<Loan> getPrestamosActivos() {
        return prestamosActivos;
    }

    public ColaPrioridad<LoanRequest> getWaitlist() {
        return waitlist;
    }

    public Graph<User> getRedAfinidad() {
        return redAfinidad;
    }
} 