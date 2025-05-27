package org.example;

import org.example.model.Book;
import org.example.model.ChatMessage;
import org.example.model.FriendRequest;
import org.example.model.Loan;
import org.example.model.LoanRequest;
import org.example.model.User;
import org.example.model.Rating;
import org.example.model.enums.FriendRequestStatus;
import org.example.model.enums.TipoUsuario;
import org.example.structures.BinaryTree.BinaryTree;
import org.example.structures.RedBlackTree.RedBlackTree;
import org.example.structures.colaPrioridad.ColaPrioridad;
import org.example.structures.colas.Cola;
import org.example.structures.doubleList.DoubleList;
import org.example.structures.graph.Graph;
import org.example.structures.pilas.Pila;
import org.example.model.FriendSuggestionDetail;

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
import java.util.ArrayList;
import java.util.List;

public class DigitalLibrary implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String DATA_FILE_PATH = "library_data.dat";

    private RedBlackTree<Book> catalogoLibros;
    private DoubleList<User> usuarios;
    private DoubleList<Loan> prestamosActivos;
    private ColaPrioridad<LoanRequest> waitlist;
    private Graph<User> redAfinidad;

    // Nuevos atributos para el Chat
    private Map<String, DoubleList<ChatMessage>> chatMessagesByRoom;
    private DoubleList<String> chatRoomNames;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public DigitalLibrary() {
        this.catalogoLibros = new RedBlackTree<>();
        this.usuarios = new DoubleList<>();
        this.prestamosActivos = new DoubleList<>();
        this.waitlist = new ColaPrioridad<>();
        this.redAfinidad = new Graph<>();

        this.chatMessagesByRoom = new HashMap<>();
        this.chatRoomNames = new DoubleList<>();
        
        String generalRoom = "General";
        if (!this.chatRoomNames.contains(generalRoom)) {
            this.chatRoomNames.addLast(generalRoom);
        }
        this.chatMessagesByRoom.putIfAbsent(generalRoom, new DoubleList<>());
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

            // Asegurar inicialización de campos de chat después de la carga
            if (library.chatMessagesByRoom == null) {
                library.chatMessagesByRoom = new HashMap<>();
            }
            if (library.chatRoomNames == null) {
                library.chatRoomNames = new DoubleList<>();
            }
            
            String generalRoom = "General";
            boolean generalExistsInNames = false;
            if (library.chatRoomNames != null) { // Comprobar nulidad antes de iterar
                for(int i = 0; i < library.chatRoomNames.size(); i++){
                    if(library.chatRoomNames.get(i).equalsIgnoreCase(generalRoom)){
                        generalExistsInNames = true;
                        break;
                    }
                }
            }
            if (!generalExistsInNames) {
                 if (library.chatRoomNames == null) library.chatRoomNames = new DoubleList<>(); // Asegurar que no sea null
                 library.chatRoomNames.addLast(generalRoom);
            }
            library.chatMessagesByRoom.putIfAbsent(generalRoom, new DoubleList<>());

            // **NUEVO: Asegurar inicialización de listas de FriendRequest en Users después de la carga**
            if (library.usuarios != null) {
                for (int i = 0; i < library.usuarios.size(); i++) {
                    User user = library.usuarios.get(i);
                    if (user != null) {
                        // Aquí es donde debemos asegurarnos de que las listas internas de User no sean null.
                        // Como User las inicializa en su constructor, este problema solo debería ocurrir
                        // con datos serializados ANTES de que User tuviera esos campos.
                        // Necesitamos una forma de inicializarlos si son null post-deserialización.
                        // La forma más directa es añadir un método en User o hacerlo aquí si tenemos acceso
                        // a los campos o a través de setters (que no tenemos para las listas directamente).

                        // Solución: Llamar a los getters y si son null, instanciar una nueva lista
                        // y asignarla. Esto requiere que User tenga setters para estas listas
                        // o que modifiquemos User para que los getters los auto-inicien.
                        // Vamos a modificar User para que tenga un método de "ensureFriendRequestListsInitialized"
                        // o hacerlo directamente aquí si es simple.

                        // Opción más simple aquí (asumiendo que User.java ya inicializa en constructor para NUEVOS User):
                        // Si user.getSentFriendRequests() es null, esto es un User "antiguo".
                        // Necesitamos crear las listas para él.
                        user.initializeFriendRequestLists(); // Llamamos al método que hemos añadido a User
                    }
                }
            }
            // El problema es que la lista es null EN EL OBJETO USER. No en DigitalLibrary.
            // La corrección principal debe estar en getFriendSuggestionsByBookCategory y en los getters de User, o User debe auto-repararse.
            // Se corregirá en getFriendSuggestionsByBookCategory.

            return library;
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error al cargar los datos de la biblioteca: " + e.getMessage());
            System.out.println("Creando una nueva instancia de DigitalLibrary debido a error de carga.");
            return new DigitalLibrary(); 
        }
    }

    // --- Métodos del Chat ---
    public synchronized void sendChatMessage(String roomName, ChatMessage message) {
        if (message == null || roomName == null || roomName.trim().isEmpty()) {
            System.err.println("Error: Mensaje o nombre de sala inválido.");
            return;
        }
        String normalizedRoomName = roomName.trim(); 
        ChatMessage messageForRoom = new ChatMessage(message.getSenderUsername(), normalizedRoomName, message.getContent());

        chatMessagesByRoom.putIfAbsent(normalizedRoomName, new DoubleList<>());
        // Añadir a la lista de nombres de salas si es realmente nueva y no solo un caso diferente
        boolean existsInKnownNames = false;
        for(int i=0; i < this.chatRoomNames.size(); i++){
            if(this.chatRoomNames.get(i).equalsIgnoreCase(normalizedRoomName)){
                existsInKnownNames = true;
                break;
            }
        }
        if(!existsInKnownNames){
            this.chatRoomNames.addLast(normalizedRoomName); 
        }

        DoubleList<ChatMessage> messages = chatMessagesByRoom.get(normalizedRoomName);
        messages.addLast(messageForRoom); // Ahora messages no debería ser null
        System.out.println("Mensaje enviado a la sala '" + normalizedRoomName + "' por '" + message.getSenderUsername() + "'");
    }

    public DoubleList<ChatMessage> getChatMessages(String roomName) {
        if (roomName == null) return new DoubleList<>();
        return chatMessagesByRoom.getOrDefault(roomName.trim(), new DoubleList<>());
    }

    public DoubleList<String> getChatRoomNames() {
        // Construye una lista actualizada de salas para la GUI
        DoubleList<String> currentDisplayableRooms = new DoubleList<>();
        currentDisplayableRooms.addLast("General"); // "General" siempre primero

        if (catalogoLibros != null) {
            DoubleList<Book> allBooks = catalogoLibros.inOrderTraversal();
            for (int i = 0; i < allBooks.size(); i++) {
                Book book = allBooks.get(i);
                if (book != null && book.getCategoria() != null && !book.getCategoria().trim().isEmpty()) {
                    String category = book.getCategoria().trim();
                    String formattedCategory = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();

                    if (!formattedCategory.equalsIgnoreCase("General")) {
                        boolean exists = false;
                        for(int j=0; j < currentDisplayableRooms.size(); j++){
                            if(currentDisplayableRooms.get(j).equalsIgnoreCase(formattedCategory)){
                                exists = true;
                                break;
                            }
                        }
                        if (!exists) {
                            currentDisplayableRooms.addLast(formattedCategory);
                        }
                    }
                }
            }
        }
        // Sincronizar la lista `this.chatRoomNames` (la que se persiste) 
        // con las salas actualmente mostrables y asegurar que todas tengan una entrada en chatMessagesByRoom.
        // Esto es importante para la consistencia si se cargan datos o se añaden/eliminan libros.
        
        DoubleList<String> finalRoomNames = new DoubleList<>();
        // Añadir todas las de currentDisplayableRooms a finalRoomNames y a this.chatMessagesByRoom
        for (int i = 0; i < currentDisplayableRooms.size(); i++) {
            String room = currentDisplayableRooms.get(i);
            finalRoomNames.addLast(room);
            this.chatMessagesByRoom.putIfAbsent(room, new DoubleList<>());
        }

        // Sincronizar this.chatRoomNames con finalRoomNames
        // Primero, eliminar de this.chatRoomNames las que ya no están en finalRoomNames (excepto si hay mensajes)
        // Esto es complejo, por ahora, simplemente reconstruimos this.chatRoomNames basado en lo que es visible.
        // La forma más simple es que this.chatRoomNames sea la lista autoritativa de las salas que *deberían* existir.
        // Y la GUI solo muestre estas.
        // Dejemos la reconstrucción de this.chatRoomNames como estaba, ya que es la que se guarda.
        // El método de la GUI llamará a este para obtener la *lista a mostrar*.
        
        // Reconstruir this.chatRoomNames para asegurar consistencia con lo que se muestra y persiste
        this.chatRoomNames.clear();
        for(int i=0; i < currentDisplayableRooms.size(); i++){
            this.chatRoomNames.addLast(currentDisplayableRooms.get(i));
            this.chatMessagesByRoom.putIfAbsent(currentDisplayableRooms.get(i), new DoubleList<>()); // Asegurar que exista en el map
        }
        return this.chatRoomNames;
    }

    // --- Gestión de Libros ---
    public void addBook(Book book) {
        if (book != null) {
            catalogoLibros.insert(book);
            if (book.getCategoria() != null && !book.getCategoria().trim().isEmpty()) {
                String category = book.getCategoria().trim();
                String formattedCategory = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
                
                chatMessagesByRoom.putIfAbsent(formattedCategory, new DoubleList<>());
                // Actualizar chatRoomNames si esta categoría es nueva
                boolean existsInKnownNames = false;
                for(int i=0; i < this.chatRoomNames.size(); i++){
                    if(this.chatRoomNames.get(i).equalsIgnoreCase(formattedCategory)){
                        existsInKnownNames = true;
                        break;
                    }
                }
                if(!existsInKnownNames){
                    this.chatRoomNames.addLast(formattedCategory);
                }
            }
        }
    }

    public Book findBookByTitle(String title) {
        DoubleList<Book> todosLosLibros = catalogoLibros.inOrderTraversal();
        for (int i = 0; i < todosLosLibros.size(); i++) {
            Book libro = todosLosLibros.get(i);
            if (libro.getTitulo().equalsIgnoreCase(title) && libro.isAvailable()) {
                return libro;
            }
        }
        return null;
    }
    
    public Book findBookById(String bookId) {
        DoubleList<Book> todosLosLibros = catalogoLibros.inOrderTraversal();
        for (int i = 0; i < todosLosLibros.size(); i++) {
            Book libro = todosLosLibros.get(i);
            if (libro.getId().equals(bookId) && libro.isAvailable()) {
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
            return "Libro no encontrado o no disponible.";
        }
        if (!book.isAvailable()) {
            return "El libro '" + book.getTitulo() + "' no está disponible actualmente.";
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
            return "Libro no encontrado o no disponible.";
        }
        if (!book.isAvailable()) {
            return "El libro '" + book.getTitulo() + "' no está disponible actualmente.";
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
                // Esto no debería pasar si loanToRemove no es null y se encontró
                return "Error interno: No se pudo determinar el índice del préstamo a remover.";
            }
            
            // Marcar como devuelto y añadir al historial del usuario
            loanToRemove.setDevuelto(true);
            loanToRemove.setFechaDevolucionReal(LocalDate.now().format(DATE_FORMATTER)); // Registrar fecha de devolución real
            user.getHistorialPrestamos().addLast(loanToRemove); // <-- AÑADIR ESTO
            
            book.devolverEjemplar(); 
            updateAffinityNetwork(user, book); // Ahora el historial estará actualizado para esta lógica
            processWaitlistForBook(book);
            return "Libro '" + book.getTitulo() + "' devuelto exitosamente.";
        } else {
            return "No se encontró un préstamo activo para este usuario y libro.";
        }
    }

    public synchronized String valorarLibro(String username, String bookId, int puntuacion, String comentario) {
        User user = findUserByUsername(username);
        if (user == null) {
            return "Error: Usuario '" + username + "' no encontrado.";
        }
        if (user.getTipoUsuario() != TipoUsuario.LECTOR) {
            return "Error: Solo los usuarios de tipo LECTOR pueden valorar libros.";
        }

        Book book = findBookById(bookId);
        if (book == null || !book.isAvailable()) {
            return "Error: Libro con ID '" + bookId + "' no encontrado o no disponible.";
        }

        // Verificar si el usuario ha prestado el libro alguna vez
        boolean haPrestadoElLibro = false;
        for (int i = 0; i < user.getHistorialPrestamos().size(); i++) {
            Loan prestamo = user.getHistorialPrestamos().get(i);
            if (prestamo.getLibro().getId().equals(bookId)) {
                haPrestadoElLibro = true;
                break;
            }
        }
        if (!haPrestadoElLibro) {
            return "Error: Debes haber tomado prestado el libro '" + book.getTitulo() + "' para poder valorarlo.";
        }

        Rating ratingExistente = null;
        for (int i = 0; i < user.getLibrosValorados().size(); i++) {
            Rating r = user.getLibrosValorados().get(i);
            if (r.getLibro().getId().equals(bookId)) {
                ratingExistente = r;
                break;
            }
        }

        String resultado;
        if (ratingExistente != null) {
            ratingExistente.setPuntuacion(puntuacion);
            ratingExistente.setComentario(comentario);
            ratingExistente.setFechaValoracion(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            resultado = "Has actualizado tu valoración para el libro '" + book.getTitulo() + "'.";
        } else {
            Rating nuevoRating = new Rating(user, book, puntuacion, comentario, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            user.getLibrosValorados().addLast(nuevoRating);
            book.getRatings().addLast(nuevoRating);
            resultado = "Libro '" + book.getTitulo() + "' valorado exitosamente con " + puntuacion + " estrellas.";
        }

        book.recalculateCalificacionPromedio();
        saveData(DATA_FILE_PATH);
        recalculateAffinitiesBasedOnRatings();
        return resultado;
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
        System.out.println("\n[DEBUG] getBookRecommendations para: " + username);
        User user = findUserByUsername(username);
        if (user == null) {
            System.out.println("[DEBUG] Usuario no encontrado.");
            return new DoubleList<>();
        }

        DoubleList<Book> recommendations = new DoubleList<>();
        DoubleList<Rating> userRatings = user.getLibrosValorados();
        DoubleList<Loan> userLoanHistory = user.getHistorialPrestamos();
        
        System.out.println("[DEBUG] Ratings del usuario (" + userRatings.size() + "): ");
        for(int i=0; i<userRatings.size(); i++) { System.out.println("  - " + userRatings.get(i).getLibro().getTitulo() + " P:" + userRatings.get(i).getPuntuacion());}
        System.out.println("[DEBUG] Historial de préstamos del usuario (" + userLoanHistory.size() + "): ");
        for(int i=0; i<userLoanHistory.size(); i++) { System.out.println("  - " + userLoanHistory.get(i).getLibro().getTitulo());}

        if (userRatings.isEmpty()) {
            System.out.println("[DEBUG] No hay ratings, llamando a fallback.");
            return getTopRatedBooksNotRatedByUserOrRead(user, 5);
        }

        DoubleList<String> favoriteCategories = new DoubleList<>();
        DoubleList<String> favoriteAuthors = new DoubleList<>();

        for (int i = 0; i < userRatings.size(); i++) {
            Rating rating = userRatings.get(i);
            if (rating.getPuntuacion() >= 3) { 
                Book ratedBook = rating.getLibro();
                if (ratedBook == null) continue;
                System.out.println("[DEBUG] Libro bien valorado: " + ratedBook.getTitulo() + " (Cat: " + ratedBook.getCategoria() + ", Autor: " + ratedBook.getAutor() + ")");
                
                boolean catExists = false;
                for(int j=0; j<favoriteCategories.size(); j++){
                    if(favoriteCategories.get(j).equalsIgnoreCase(ratedBook.getCategoria())){ catExists = true; break; }
                }
                if(!catExists) favoriteCategories.addLast(ratedBook.getCategoria());

                boolean authorExists = false;
                for(int j=0; j<favoriteAuthors.size(); j++){
                    if(favoriteAuthors.get(j).equalsIgnoreCase(ratedBook.getAutor())){ authorExists = true; break; }
                }
                if(!authorExists) favoriteAuthors.addLast(ratedBook.getAutor());
            }
        }
        System.out.println("[DEBUG] Categorías favoritas: " + favoriteCategories.size()); // Idealmente mostrar contenido
        for(int i=0; i<favoriteCategories.size(); i++) { System.out.println("  Cat: " + favoriteCategories.get(i)); }
        System.out.println("[DEBUG] Autores favoritos: " + favoriteAuthors.size()); // Idealmente mostrar contenido
        for(int i=0; i<favoriteAuthors.size(); i++) { System.out.println("  Aut: " + favoriteAuthors.get(i)); }

        if (favoriteCategories.isEmpty() && favoriteAuthors.isEmpty()) {
            System.out.println("[DEBUG] No hay categorías/autores favoritos, llamando a fallback.");
            return getTopRatedBooksNotRatedByUserOrRead(user, 5);
        }

        DoubleList<Book> allBooks = catalogoLibros.inOrderTraversal();
        System.out.println("[DEBUG] Procesando " + allBooks.size() + " libros del catálogo para recomendaciones.");
        for (int i = 0; i < allBooks.size(); i++) {
            Book potentialRecommendation = allBooks.get(i);
            if (potentialRecommendation == null || !potentialRecommendation.isAvailable()) continue;
            System.out.println("  [DEBUG] Considernado: " + potentialRecommendation.getTitulo());

            boolean alreadyRecommended = false;
            for(int k=0; k < recommendations.size(); k++){
                if(recommendations.get(k).equals(potentialRecommendation)){ alreadyRecommended = true; break;}
            }
            if(alreadyRecommended) { System.out.println("    [DEBUG] Rechazado: Ya en esta lista de recomendación."); continue; }

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
                System.out.println("    [DEBUG] ACEPTADO para recomendación: " + potentialRecommendation.getTitulo() + " (Match Cat: "+matchesCategory+", Match Aut: "+matchesAuthor+")");
                recommendations.addLast(potentialRecommendation);
            } else {
                 System.out.println("    [DEBUG] Rechazado: No coincide categoría/autor favorito.");
            }
        }
        
        System.out.println("[DEBUG] Recomendaciones preliminares (" + recommendations.size() + ") antes de ordenar y limitar:");
        for(int i=0; i<recommendations.size(); i++) { System.out.println("  - " + recommendations.get(i).getTitulo());}

        if (!recommendations.isEmpty()) {
            for (int i = 0; i < recommendations.size() - 1; i++) {
                for (int j = 0; j < recommendations.size() - i - 1; j++) {
                    if (recommendations.get(j).getCalificacionPromedio() < recommendations.get(j + 1).getCalificacionPromedio()) {
                        Book temp = recommendations.get(j);
                        recommendations.set(j, recommendations.get(j + 1)); 
                        recommendations.set(j + 1, temp);
                    }
                }
            }
        }
        
        DoubleList<Book> finalRecommendations = new DoubleList<>();
        for(int i=0; i < recommendations.size() && i < 5; i++){ 
            finalRecommendations.addLast(recommendations.get(i));
        }
        System.out.println("[DEBUG] Recomendaciones finales (" + finalRecommendations.size() + "):");
        for(int i=0; i<finalRecommendations.size(); i++) { System.out.println("  - " + finalRecommendations.get(i).getTitulo());}

        if (finalRecommendations.isEmpty()){
            System.out.println("[DEBUG] No hay recomendaciones personalizadas, llamando a fallback final.");
            return getTopRatedBooksNotRatedByUserOrRead(user, 5);
        }
        return finalRecommendations;
    }

    // Método auxiliar modificado para obtener los N libros mejor valorados que el usuario NO HA VALORADO NI LEÍDO
    private DoubleList<Book> getTopRatedBooksNotRatedByUserOrRead(User user, int count) {
        System.out.println("[DEBUG] Entrando a getTopRatedBooksNotRatedByUserOrRead para user: " + user.getUsername());
        DoubleList<Book> allBooks = catalogoLibros.inOrderTraversal();
        DoubleList<Book> booksToExclude = new DoubleList<>();

        // Añadir libros valorados a la lista de exclusión
        for(int i=0; i < user.getLibrosValorados().size(); i++){
            Book ratedBook = user.getLibrosValorados().get(i).getLibro();
            if (ratedBook != null) {
                boolean exists = false;
                for(int k=0; k<booksToExclude.size(); k++){ if(booksToExclude.get(k).equals(ratedBook)) {exists=true; break;}}
                if(!exists) booksToExclude.addLast(ratedBook);
            }
        }
        // Añadir libros del historial de préstamos a la lista de exclusión
        for(int i=0; i < user.getHistorialPrestamos().size(); i++){
            Book loanedBook = user.getHistorialPrestamos().get(i).getLibro();
            if (loanedBook != null) {
                boolean exists = false;
                for(int k=0; k<booksToExclude.size(); k++){ if(booksToExclude.get(k).equals(loanedBook)) {exists=true; break;}}
                if(!exists) booksToExclude.addLast(loanedBook);
            }
        }

        DoubleList<Book> candidateBooks = new DoubleList<>();
        for(int i=0; i < allBooks.size(); i++){
            Book currentBook = allBooks.get(i);
            if (currentBook == null || !currentBook.isAvailable()) continue;
            boolean exclude = false;
            if(booksToExclude.contains(currentBook)){
                 exclude = true;
            }
            if(!exclude) {
                candidateBooks.addLast(currentBook);
            }
        }

        if (!candidateBooks.isEmpty()) {
            for (int i = 0; i < candidateBooks.size() - 1; i++) {
                for (int j = 0; j < candidateBooks.size() - i - 1; j++) {
                    if (candidateBooks.get(j).getCalificacionPromedio() < candidateBooks.get(j + 1).getCalificacionPromedio()) {
                        Book temp = candidateBooks.get(j);
                        candidateBooks.set(j, candidateBooks.get(j + 1));
                        candidateBooks.set(j + 1, temp);
                    }
                }
            }
        }
        
        System.out.println("[DEBUG] Fallback - libros candidatos (" + candidateBooks.size() + ") antes de top " + count + ":");
        for(int i=0; i<candidateBooks.size(); i++) { System.out.println("  - " + candidateBooks.get(i).getTitulo() + " Rating: " + candidateBooks.get(i).getCalificacionPromedio());}

        DoubleList<Book> topBooks = new DoubleList<>();
        for(int i=0; i < candidateBooks.size() && i < count; i++){
            topBooks.addLast(candidateBooks.get(i));
        }
        System.out.println("[DEBUG] Fallback - top libros (" + topBooks.size() + "):");
        for(int i=0; i<topBooks.size(); i++) { System.out.println("  - " + topBooks.get(i).getTitulo());}
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

    // --- Métodos para Administrador ---

    public synchronized String deleteBook(String bookId) {
        Book bookToDelete = findBookById(bookId);
        if (bookToDelete == null) {
            return "Error: Libro con ID " + bookId + " no encontrado en el catálogo.";
        }

        // Verificar si el libro está en préstamos activos
        for (int i = 0; i < prestamosActivos.size(); i++) {
            if (prestamosActivos.get(i).getLibro().equals(bookToDelete)) {
                return "Error: El libro '" + bookToDelete.getTitulo() + "' tiene préstamos activos y no puede ser marcado como no disponible.";
            }
        }

        // Verificar si el libro está en la lista de espera
        ColaPrioridad<LoanRequest> tempWaitlist = new ColaPrioridad<>();
        boolean onWaitlist = false;
        while(!waitlist.isEmpty()){
            LoanRequest req = waitlist.poll();
            if(req.getBook().equals(bookToDelete)){
                onWaitlist = true;
            }
            tempWaitlist.add(req);
        }
        while(!tempWaitlist.isEmpty()){
            waitlist.add(tempWaitlist.poll());
        }

        if (onWaitlist) {
            return "Error: El libro '" + bookToDelete.getTitulo() + "' está en la lista de espera y no puede ser marcado como no disponible.";
        }

        // Marcar el libro como no disponible
        bookToDelete.setAvailable(false);
        return "Libro '" + bookToDelete.getTitulo() + "' marcado como no disponible.";
    }

    public java.util.Map<String, Integer> getLoanCountsPerUser() {
        java.util.Map<String, Integer> loanCounts = new java.util.HashMap<>();
        for (int i = 0; i < usuarios.size(); i++) {
            User user = usuarios.get(i);
            int count = 0;
            // Contar préstamos activos
            for (int j = 0; j < prestamosActivos.size(); j++) {
                if (prestamosActivos.get(j).getUsuario().equals(user)) {
                    count++;
                }
            }
            // Contar préstamos en el historial
            if (user.getHistorialPrestamos() != null) {
                count += user.getHistorialPrestamos().size();
            }
            loanCounts.put(user.getUsername(), count);
        }
        return loanCounts;
    }

    public DoubleList<Book> getMostRatedBooks(int count) {
        DoubleList<Book> allBooksFromCatalog = catalogoLibros.inOrderTraversal();
        DoubleList<Book> availableBooks = new DoubleList<>();
        for(int i=0; i<allBooksFromCatalog.size(); i++){
            Book book = allBooksFromCatalog.get(i);
            if(book.isAvailable()){ // Considerar solo libros disponibles
                availableBooks.addLast(book);
            }
        }

        // Ordenar libros por calificación promedio (descendente)
        if (!availableBooks.isEmpty()) {
            for (int i = 0; i < availableBooks.size() - 1; i++) {
                for (int j = 0; j < availableBooks.size() - i - 1; j++) {
                    if (availableBooks.get(j).getCalificacionPromedio() < availableBooks.get(j + 1).getCalificacionPromedio()) {
                        Book temp = availableBooks.get(j);
                        availableBooks.set(j, availableBooks.get(j + 1));
                        availableBooks.set(j + 1, temp);
                    }
                }
            }
        }

        DoubleList<Book> topBooks = new DoubleList<>();
        for (int i = 0; i < availableBooks.size() && i < count; i++) {
            topBooks.addLast(availableBooks.get(i));
        }
        return topBooks;
    }

    public java.util.Map<String, Integer> getUsersWithMostConnections(int count) {
        java.util.Map<String, Integer> userConnections = new java.util.HashMap<>();
        if (redAfinidad == null || usuarios.isEmpty()) { 
            return userConnections; // Grafo vacío o no inicializado, o no hay usuarios
        }

        for (int i = 0; i < usuarios.size(); i++) {
            User user = usuarios.get(i);
            DoubleList<User> neighbors = redAfinidad.getNeighbors(user);
            userConnections.put(user.getUsername(), neighbors != null ? neighbors.size() : 0);
        }

        // Ordenar el mapa por valores (número de conexiones) de forma descendente
        java.util.List<java.util.Map.Entry<String, Integer>> list = new java.util.ArrayList<>(userConnections.entrySet());
        list.sort(java.util.Map.Entry.<String, Integer>comparingByValue().reversed());

        java.util.Map<String, Integer> topUsers = new java.util.LinkedHashMap<>(); // Para mantener el orden de inserción
        for (int i = 0; i < list.size() && i < count; i++) {
            java.util.Map.Entry<String, Integer> entry = list.get(i);
            topUsers.put(entry.getKey(), entry.getValue());
        }
        return topUsers;
    }

    public synchronized String deleteUser(String usernameToDelete) {
        User userToDelete = findUserByUsername(usernameToDelete);

        if (userToDelete == null) {
            return "Error: Usuario '" + usernameToDelete + "' no encontrado.";
        }

        // Opcional: Impedir que un admin se elimine a sí mismo si está logueado
        // if (currentUserLoggedIn != null && currentUserLoggedIn.equals(userToDelete) && currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
        //     return "Error: Un administrador no puede eliminarse a sí mismo.";
        // }

        // 1. Verificar préstamos activos
        for (int i = 0; i < prestamosActivos.size(); i++) {
            Loan loan = prestamosActivos.get(i);
            if (loan.getUsuario().equals(userToDelete)) {
                return "Error: El usuario '" + usernameToDelete + "' tiene préstamos activos. No se puede eliminar.";
            }
        }

        // 2. Eliminar valoraciones de este usuario de todos los libros
        DoubleList<Book> allBooks = catalogoLibros.inOrderTraversal();
        for (int i = 0; i < allBooks.size(); i++) {
            Book book = allBooks.get(i);
            DoubleList<Rating> ratingsDelLibro = book.getRatings();
            DoubleList<Rating> ratingsAGuardar = new DoubleList<>(); // Nueva lista para ratings que no son del usuario
            boolean bookRatingsModified = false;
            for (int j = 0; j < ratingsDelLibro.size(); j++) {
                Rating rating = ratingsDelLibro.get(j);
                if (!rating.getUsuario().equals(userToDelete)) {
                    ratingsAGuardar.addLast(rating);
                } else {
                    bookRatingsModified = true;
                }
            }
            if (bookRatingsModified) {
                // Reemplazar la lista de ratings del libro y recalcular promedio
                // Esto asume que Book tiene un setRatings(DoubleList<Rating>) o que podemos limpiar y re-añadir
                // Por ahora, vamos a limpiar y re-añadir.
                ratingsDelLibro.clear(); // Asume que DoubleList tiene clear()
                for(int k=0; k < ratingsAGuardar.size(); k++){
                    ratingsDelLibro.addLast(ratingsAGuardar.get(k));
                }
                book.recalculateCalificacionPromedio(); // Asume que Book tiene este método
            }
        }

        // 3. Eliminar historial de préstamos y valoraciones DEL USUARIO
        if (userToDelete.getHistorialPrestamos() != null) {
            userToDelete.getHistorialPrestamos().clear(); // Asume clear()
        }
        if (userToDelete.getLibrosValorados() != null) {
            userToDelete.getLibrosValorados().clear(); // Asume clear()
        }

        // 4. Eliminar del grafo de afinidad
        redAfinidad.removeVertex(userToDelete); // Asume que Graph tiene removeVertex(T vertex)

        // 5. Eliminar de la lista de usuarios
        boolean removedFromList = usuarios.remove(userToDelete); // Asume que DoubleList tiene remove(Object o)
        
        if (removedFromList) {
            return "Usuario '" + usernameToDelete + "' eliminado exitosamente junto con su historial y valoraciones.";
        } else {
            // Esto no debería pasar si el usuario fue encontrado inicialmente, pero es una salvaguarda
            return "Error: No se pudo eliminar al usuario '" + usernameToDelete + "' de la lista principal (pero otros datos podrían haber sido afectados).";
        }
    }

    // --- Implementación de Sugerencia de Amigos por Categoría de Libros ---
    public DoubleList<String> getPreferredCategories(User user) {
        DoubleList<String> preferredCategories = new DoubleList<>();
        if (user == null) return preferredCategories;

        DoubleList<Loan> loanHistory = user.getHistorialPrestamos();
        if (loanHistory != null) {
            for (int i = 0; i < loanHistory.size(); i++) {
                Loan loan = loanHistory.get(i);
                if (loan != null && loan.getLibro() != null) {
                    Book book = loan.getLibro();
                    if (book.getCategoria() != null && !book.getCategoria().trim().isEmpty()) {
                        String category = book.getCategoria().trim();
                        String formattedCategory = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
                        if (!preferredCategories.contains(formattedCategory)) {
                            preferredCategories.addLast(formattedCategory);
                        }
                    }
                }
            }
        }

        DoubleList<Rating> ratings = user.getLibrosValorados();
        if (ratings != null) {
            for (int i = 0; i < ratings.size(); i++) {
                Rating rating = ratings.get(i);
                if (rating != null && rating.getPuntuacion() >= 3) { 
                    Book book = rating.getLibro();
                    if (book != null && book.getCategoria() != null && !book.getCategoria().trim().isEmpty()) {
                        String category = book.getCategoria().trim();
                        String formattedCategory = category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase();
                        if (!preferredCategories.contains(formattedCategory)) {
                            preferredCategories.addLast(formattedCategory);
                        }
                    }
                }
            }
        }
        return preferredCategories;
    }

    public DoubleList<FriendSuggestionDetail> getFriendSuggestionsByBookCategory(String username, int count) {
        User currentUser = findUserByUsername(username);
        if (currentUser == null) return new DoubleList<>(); // Devolver lista vacía de FriendSuggestionDetail

        DoubleList<String> currentUserPreferredCategories = getPreferredCategories(currentUser);
        if (currentUserPreferredCategories.isEmpty()) return new DoubleList<>();

        // Usar una lista de Java para facilitar la ordenación con comparador
        List<FriendSuggestionDetail> suggestionDetailsList = new ArrayList<>();
        DoubleList<User> allUsers = getUsuarios();
        DoubleList<User> currentUserConnections = null;
        if(redAfinidad.hasVertex(currentUser)) { 
            currentUserConnections = redAfinidad.getNeighbors(currentUser);
        }

        for (int i = 0; i < allUsers.size(); i++) {
            User otherUser = allUsers.get(i);
            if (otherUser.equals(currentUser)) continue; 

            boolean alreadyConnected = false;
            if (currentUserConnections != null && currentUserConnections.contains(otherUser)){
                alreadyConnected = true;
            }
            if (alreadyConnected) continue;
            
            boolean requestExists = false;
            DoubleList<FriendRequest> currentUserSentRequests = currentUser.getSentFriendRequests();
            if (currentUserSentRequests != null) { 
                for(int k=0; k < currentUserSentRequests.size(); k++){ 
                    FriendRequest req = currentUserSentRequests.get(k);
                    if(req.getReceiverUsername().equals(otherUser.getUsername()) && 
                       (req.getStatus() == FriendRequestStatus.PENDING || req.getStatus() == FriendRequestStatus.ACCEPTED)){
                        requestExists = true;
                        break;
                    }
                }
            }
            if(requestExists) continue;

            DoubleList<FriendRequest> currentUserReceivedRequests = currentUser.getReceivedFriendRequests();
            if (currentUserReceivedRequests != null) { 
                for(int k=0; k < currentUserReceivedRequests.size(); k++){ 
                    FriendRequest req = currentUserReceivedRequests.get(k);
                    if(req.getSenderUsername().equals(otherUser.getUsername()) && 
                       (req.getStatus() == FriendRequestStatus.PENDING || req.getStatus() == FriendRequestStatus.ACCEPTED)){
                        requestExists = true;
                        break;
                    }
                }
            }
            if(requestExists) continue;

            DoubleList<String> otherUserPreferredCategories = getPreferredCategories(otherUser);
            DoubleList<String> commonCategoriesFound = new DoubleList<>(); // Para almacenar las categorías comunes
            int commonCategoriesCount = 0;

            for (int j = 0; j < currentUserPreferredCategories.size(); j++) {
                String preferredCat = currentUserPreferredCategories.get(j);
                if (otherUserPreferredCategories.contains(preferredCat)) {
                    commonCategoriesCount++;
                    if(!commonCategoriesFound.contains(preferredCat)) { // Asegurar unicidad si es necesario, aunque aquí solo listamos
                        commonCategoriesFound.addLast(preferredCat);
                    }
                }
            }

            if (commonCategoriesCount > 0) {
                suggestionDetailsList.add(new FriendSuggestionDetail(otherUser, commonCategoriesFound, commonCategoriesCount));
            }
        }

        // Ordenar la lista de FriendSuggestionDetail por similarityScore (descendente)
        suggestionDetailsList.sort((s1, s2) -> Integer.compare(s2.getSimilarityScore(), s1.getSimilarityScore()));

        // Convertir la List<FriendSuggestionDetail> ordenada a DoubleList<FriendSuggestionDetail> y tomar 'count' elementos
        DoubleList<FriendSuggestionDetail> finalSuggestions = new DoubleList<>();
        for (int i = 0; i < suggestionDetailsList.size() && i < count; i++) {
            finalSuggestions.addLast(suggestionDetailsList.get(i));
        }
        return finalSuggestions;
    }

    // --- Gestión de Solicitudes de Amistad ---
    public synchronized String sendFriendRequest(String senderUsername, String receiverUsername) {
        User senderUser = findUserByUsername(senderUsername);
        User receiverUser = findUserByUsername(receiverUsername);

        if (senderUser == null) return "Error: Usuario remitente no encontrado.";
        if (receiverUser == null) return "Error: Usuario destinatario no encontrado.";
        if (senderUsername.equals(receiverUsername)) return "Error: No puedes enviarte una solicitud a ti mismo.";

        if (redAfinidad.hasVertex(senderUser) && redAfinidad.getNeighbors(senderUser).contains(receiverUser)) {
            return "Error: Ya sois amigos.";
        }

        for (int i = 0; i < senderUser.getSentFriendRequests().size(); i++) {
            FriendRequest req = senderUser.getSentFriendRequests().get(i);
            if (req.getReceiverUsername().equals(receiverUsername)) {
                if (req.getStatus() == FriendRequestStatus.PENDING) return "Error: Ya existe una solicitud pendiente para este usuario.";
                if (req.getStatus() == FriendRequestStatus.ACCEPTED) return "Error: Ya sois amigos (solicitud previamente aceptada).";
            }
        }
         for (int i = 0; i < senderUser.getReceivedFriendRequests().size(); i++) {
            FriendRequest req = senderUser.getReceivedFriendRequests().get(i);
            if (req.getSenderUsername().equals(receiverUsername) && req.getStatus() == FriendRequestStatus.PENDING) {
                return "Error: Ya tienes una solicitud pendiente de este usuario. Revísala.";
            }
        }       

        FriendRequest newRequest = new FriendRequest(senderUsername, receiverUsername);
        senderUser.getSentFriendRequests().addLast(newRequest);
        receiverUser.getReceivedFriendRequests().addLast(newRequest);
        return "Solicitud de amistad enviada a " + receiverUsername + ".";
    }

    public DoubleList<FriendRequest> getPendingFriendRequests(String username) {
        User user = findUserByUsername(username);
        DoubleList<FriendRequest> pendingRequests = new DoubleList<>();
        if (user == null || user.getReceivedFriendRequests() == null) return pendingRequests;

        for (int i = 0; i < user.getReceivedFriendRequests().size(); i++) {
            FriendRequest req = user.getReceivedFriendRequests().get(i);
            if (req.getStatus() == FriendRequestStatus.PENDING) {
                pendingRequests.addLast(req);
            }
        }
        return pendingRequests;
    }
    
    public DoubleList<FriendRequest> getSentFriendRequestsWithStatus(String username) {
        User user = findUserByUsername(username);
        if (user == null || user.getSentFriendRequests() == null) {
            return new DoubleList<>(); // Devuelve lista vacía si no hay usuario o solicitudes
        }
        return user.getSentFriendRequests();
    }

    public synchronized String acceptFriendRequest(String acceptorUsername, String requesterUsername) {
        User acceptor = findUserByUsername(acceptorUsername);
        User requester = findUserByUsername(requesterUsername);

        if (acceptor == null || requester == null) {
            return "Error: Uno o ambos usuarios no existen.";
        }
        if (acceptor.equals(requester)) {
            return "Error: No puedes aceptar una solicitud de ti mismo.";
        }

        FriendRequest requestInAcceptorList = null;
        for (int i = 0; i < acceptor.getReceivedFriendRequests().size(); i++) {
            FriendRequest req = acceptor.getReceivedFriendRequests().get(i);
            if (req.getSenderUsername().equals(requesterUsername) && req.getStatus() == FriendRequestStatus.PENDING) {
                requestInAcceptorList = req;
                break;
            }
        }

        if (requestInAcceptorList == null) {
            return "No se encontró una solicitud pendiente de " + requesterUsername + " para " + acceptorUsername + ".";
        }

        requestInAcceptorList.setStatus(FriendRequestStatus.ACCEPTED);

        // Sincronizar el estado en la lista de enviados del solicitante
        for (int i = 0; i < requester.getSentFriendRequests().size(); i++) {
            FriendRequest sentReq = requester.getSentFriendRequests().get(i);
            if (sentReq.getReceiverUsername().equals(acceptorUsername) && sentReq.getSenderUsername().equals(requesterUsername)) {
                sentReq.setStatus(FriendRequestStatus.ACCEPTED);
                break;
            }
        }
        
        saveData(DATA_FILE_PATH);
        return "Solicitud de amistad de " + requesterUsername + " aceptada.";
    }

    public synchronized String rejectFriendRequest(String rejectorUsername, String requesterUsername) {
        User rejectorUser = findUserByUsername(rejectorUsername);
        User requesterUser = findUserByUsername(requesterUsername);

        if (rejectorUser == null || requesterUser == null) return "Error: Uno o ambos usuarios no encontrados.";

        FriendRequest requestToReject = null;
        int requestIndexInReceived = -1;
        // Buscar en las recibidas del que rechaza
        for (int i = 0; i < rejectorUser.getReceivedFriendRequests().size(); i++) {
            FriendRequest req = rejectorUser.getReceivedFriendRequests().get(i);
            if (req.getSenderUsername().equals(requesterUsername) && req.getStatus() == FriendRequestStatus.PENDING) {
                requestToReject = req;
                requestIndexInReceived = i;
                break;
            }
        }

        if (requestToReject == null) return "Error: No se encontró una solicitud pendiente de " + requesterUsername + " para rechazar.";

        // Opción 1: Marcar como REJECTED
        requestToReject.setStatus(FriendRequestStatus.REJECTED);
        // Actualizar también en la lista de enviados del solicitante original
         for (int i = 0; i < requesterUser.getSentFriendRequests().size(); i++) {
            FriendRequest req = requesterUser.getSentFriendRequests().get(i);
            if (req.getReceiverUsername().equals(rejectorUsername) && req.getSenderUsername().equals(requesterUsername)) { 
                req.setStatus(FriendRequestStatus.REJECTED);
                break;
            }
        }
        return "Solicitud de amistad de " + requesterUsername + " rechazada.";
    }

    public synchronized String removeFriend(String user1Username, String user2Username) {
        User user1 = findUserByUsername(user1Username);
        User user2 = findUserByUsername(user2Username);

        if (user1 == null || user2 == null) {
            return "Error: Uno o ambos usuarios no existen.";
        }

        if (user1.equals(user2)) {
            return "Error: No puedes eliminarte a ti mismo como amigo.";
        }

        boolean friendshipAltered = false;

        // Buscar en solicitudes enviadas por user1 a user2
        for (int i = 0; i < user1.getSentFriendRequests().size(); i++) {
            FriendRequest req = user1.getSentFriendRequests().get(i);
            if (req.getReceiverUsername().equals(user2Username) && req.getStatus() == FriendRequestStatus.ACCEPTED) {
                req.setStatus(FriendRequestStatus.REJECTED); // O un nuevo estado UNFRIENDED
                friendshipAltered = true;
            }
        }
        // Buscar en solicitudes recibidas por user1 de user2
        for (int i = 0; i < user1.getReceivedFriendRequests().size(); i++) {
            FriendRequest req = user1.getReceivedFriendRequests().get(i);
            if (req.getSenderUsername().equals(user2Username) && req.getStatus() == FriendRequestStatus.ACCEPTED) {
                req.setStatus(FriendRequestStatus.REJECTED);
                friendshipAltered = true;
            }
        }
        // Sincronizar para user2
        for (int i = 0; i < user2.getSentFriendRequests().size(); i++) {
            FriendRequest req = user2.getSentFriendRequests().get(i);
            if (req.getReceiverUsername().equals(user1Username) && req.getStatus() == FriendRequestStatus.ACCEPTED) {
                req.setStatus(FriendRequestStatus.REJECTED);
                friendshipAltered = true;
            }
        }
        for (int i = 0; i < user2.getReceivedFriendRequests().size(); i++) {
            FriendRequest req = user2.getReceivedFriendRequests().get(i);
            if (req.getSenderUsername().equals(user1Username) && req.getStatus() == FriendRequestStatus.ACCEPTED) {
                req.setStatus(FriendRequestStatus.REJECTED);
                friendshipAltered = true;
            }
        }

        if (friendshipAltered) {
            saveData(DATA_FILE_PATH);
            return "Amistad entre " + user1Username + " y " + user2Username + " terminada (estado de solicitud actualizado).";
        } else {
            return "No se encontró una amistad aceptada entre " + user1Username + " y " + user2Username + " para terminar.";
        }
    }

    // --- NUEVO: Lógica para afinidad automática basada en valoraciones ---
    private void clearGraphEdges(Graph<User> graphToClear) {
        if (graphToClear == null) return;
        DoubleList<User> vertexList = new DoubleList<>();
        // Copiamos los vértices para evitar ConcurrentModificationException si la estructura interna de getVertices cambia
        for(User u : graphToClear.getVertices()){
            vertexList.addLast(u);
        }

        for (int i = 0; i < vertexList.size(); i++) {
            User u1 = vertexList.get(i);
            DoubleList<User> neighbors = graphToClear.getNeighbors(u1);
            // Creamos una copia de los vecinos para iterar y modificar
            DoubleList<User> neighborsCopy = new DoubleList<>();
            for(int k=0; k < neighbors.size(); k++){
                neighborsCopy.addLast(neighbors.get(k));
            }
            for (int j = 0; j < neighborsCopy.size(); j++) {
                User u2 = neighborsCopy.get(j);
                graphToClear.removeEdge(u1, u2);
            }
        }
    }

    public void recalculateAffinitiesBasedOnRatings() {
        if (this.usuarios == null || this.usuarios.isEmpty()) {
            System.out.println("DEBUG: No hay usuarios para calcular afinidades.");
            return;
        }

        if (redAfinidad == null) {
            redAfinidad = new Graph<>();
        } else {
            // Primero, eliminamos todas las aristas existentes que representaban afinidades previas.
            // Los vértices (usuarios) se mantienen.
            clearGraphEdges(redAfinidad);
        }

        DoubleList<User> lectores = new DoubleList<>();
        for (int i = 0; i < usuarios.size(); i++) {
            User u = usuarios.get(i);
            if (u.getTipoUsuario() == TipoUsuario.LECTOR) {
                lectores.addLast(u);
                redAfinidad.addVertex(u); // Asegurarse de que todos los lectores son vértices
            }
        }

        for (int i = 0; i < lectores.size(); i++) {
            User userA = lectores.get(i);
            for (int j = i + 1; j < lectores.size(); j++) {
                User userB = lectores.get(j);

                if (userA.equals(userB)) continue;

                DoubleList<Rating> ratingsA = userA.getLibrosValorados();
                DoubleList<Rating> ratingsB = userB.getLibrosValorados();

                if (ratingsA.isEmpty() || ratingsB.isEmpty()) continue;

                int commonBooksWithSimilarRating = 0;

                for (int k = 0; k < ratingsA.size(); k++) {
                    Rating ratingA = ratingsA.get(k);
                    Book bookA = ratingA.getLibro();
                    if (bookA == null) continue;

                    for (int l = 0; l < ratingsB.size(); l++) {
                        Rating ratingB = ratingsB.get(l);
                        Book bookB = ratingB.getLibro();
                        if (bookB == null) continue;

                        if (bookA.getId().equals(bookB.getId())) {
                            // Libro en común encontrado
                            int diff = Math.abs(ratingA.getPuntuacion() - ratingB.getPuntuacion());
                            if (diff <= 1) { // Calificaciones similares (diferencia de 0 o 1)
                                commonBooksWithSimilarRating++;
                            }
                            break; // Pasar al siguiente rating de userA
                        }
                    }
                }

                if (commonBooksWithSimilarRating >= 3) {
                    redAfinidad.addEdge(userA, userB);
                    System.out.println("DEBUG: Afinidad automática creada entre " + userA.getUsername() + " y " + userB.getUsername());
                }
            }
        }
        System.out.println("DEBUG: Recálculo de afinidades automáticas completado.");
        saveData(DATA_FILE_PATH); // Guardar cambios en la red de afinidad
    }
    
    public String adminRecalculateAllAffinities() {
        recalculateAffinitiesBasedOnRatings();
        return "Se han recalculado todas las afinidades automáticas basadas en valoraciones.";
    }

    // --- Implementación de Sugerencia de Amigos por Red (Amigos de Amigos) ---
    public DoubleList<User> getFriendSuggestionsByNetwork(String username, int count) {
        DoubleList<User> suggestions = new DoubleList<>();
        User currentUser = findUserByUsername(username);

        if (currentUser == null || !redAfinidad.hasVertex(currentUser)) {
            return suggestions; // Usuario no encontrado o no en el grafo
        }

        DoubleList<User> directConnections = redAfinidad.getNeighbors(currentUser);
        DoubleList<User> candidates = new DoubleList<>();

        // 1. Obtener amigos de amigos (vecinos de segundo grado)
        for (int i = 0; i < directConnections.size(); i++) {
            User friend = directConnections.get(i);
            DoubleList<User> friendsOfFriend = redAfinidad.getNeighbors(friend);
            for (int j = 0; j < friendsOfFriend.size(); j++) {
                User potentialSuggestion = friendsOfFriend.get(j);
                // Añadir si no es el usuario actual y no es ya un amigo directo
                if (!potentialSuggestion.equals(currentUser) && !directConnections.contains(potentialSuggestion)) {
                    if (!candidates.contains(potentialSuggestion)) { // Evitar duplicados en la lista de candidatos
                        candidates.addLast(potentialSuggestion);
                    }
                }
            }
        }

        // 2. Filtrar adicionalmente si es necesario (ej. solicitudes pendientes/aceptadas del sistema de amistad explícito)
        // Por ahora, mantenemos la sugerencia basada puramente en la estructura del grafo de gustos.
        // Si quisiéramos filtrar por FriendRequests (del sistema de amistad separado):
        /*
        DoubleList<User> finalSuggestions = new DoubleList<>();
        for (int i = 0; i < candidates.size(); i++) {
            User candidate = candidates.get(i);
            boolean hasPendingOrAcceptedRequest = false;
            // Revisar sentFriendRequests del currentUser hacia el candidate
            for (int sr = 0; sr < currentUser.getSentFriendRequests().size(); sr++) {
                FriendRequest sentReq = currentUser.getSentFriendRequests().get(sr);
                if (sentReq.getReceiverUsername().equals(candidate.getUsername()) && 
                    (sentReq.getStatus() == FriendRequestStatus.PENDING || sentReq.getStatus() == FriendRequestStatus.ACCEPTED)) {
                    hasPendingOrAcceptedRequest = true;
                    break;
                }
            }
            if (hasPendingOrAcceptedRequest) continue;

            // Revisar receivedFriendRequests del currentUser desde el candidate
            for (int rr = 0; rr < currentUser.getReceivedFriendRequests().size(); rr++) {
                FriendRequest recReq = currentUser.getReceivedFriendRequests().get(rr);
                if (recReq.getSenderUsername().equals(candidate.getUsername()) && 
                    (recReq.getStatus() == FriendRequestStatus.PENDING || recReq.getStatus() == FriendRequestStatus.ACCEPTED)) {
                    hasPendingOrAcceptedRequest = true;
                    break;
                }
            }
            if (!hasPendingOrAcceptedRequest) {
                finalSuggestions.addLast(candidate);
            }
        }
        // Devolver 'count' sugerencias de finalSuggestions
        for (int i = 0; i < finalSuggestions.size() && suggestions.size() < count; i++) {
            suggestions.addLast(finalSuggestions.get(i));
        }
        */

        // Devolver 'count' sugerencias de los candidatos (sin el filtro de FriendRequest por ahora)
        for (int i = 0; i < candidates.size() && suggestions.size() < count; i++) {
            suggestions.addLast(candidates.get(i));
        }

        return suggestions;
    }
} 