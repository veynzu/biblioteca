package org.example.model;

import org.example.model.enums.TipoUsuario;
import org.example.structures.doubleList.DoubleList; // Importar DoubleList
import java.io.Serializable; // Importar Serializable
import org.example.model.FriendRequest; // Importar FriendRequest
// import co.edu.uniquindio.estructuradato.proyectofinal.structures.listasSimples.ListaPropia; // Se añadirá después

public class User extends Person implements Comparable <User>, Serializable {
    private static final long serialVersionUID = 1L; // Buena práctica

    private String username; // Para login
    private String password; // Considerar hashear en la práctica
    private TipoUsuario tipoUsuario;
    private DoubleList<Loan> historialPrestamos; // Usar DoubleList
    private DoubleList<Rating> librosValorados;  // Usar DoubleList

    // Nuevos campos para solicitudes de amistad
    private DoubleList<FriendRequest> sentFriendRequests;
    private DoubleList<FriendRequest> receivedFriendRequests;

    public User() {
        super();
        this.historialPrestamos = new DoubleList<>(); // Inicializar
        this.librosValorados = new DoubleList<>();  // Inicializar
        this.sentFriendRequests = new DoubleList<>(); // Inicializar
        this.receivedFriendRequests = new DoubleList<>(); // Inicializar
    }

    public User(String idPersona, String nombre, String apellido, String email, 
                String username, String password, TipoUsuario tipoUsuario) {
        super(idPersona, nombre, apellido, email);
        this.username = username;
        this.password = password;
        this.tipoUsuario = tipoUsuario;
        this.historialPrestamos = new DoubleList<>(); // Inicializar
        this.librosValorados = new DoubleList<>();  // Inicializar
        this.sentFriendRequests = new DoubleList<>(); // Inicializar
        this.receivedFriendRequests = new DoubleList<>(); // Inicializar
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public TipoUsuario getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(TipoUsuario tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
    }

    public DoubleList<Loan> getHistorialPrestamos() { // Getter
        return historialPrestamos;
    }

    public DoubleList<Rating> getLibrosValorados() { // Getter
        return librosValorados;
    }

    // Getters para las nuevas listas de solicitudes de amistad
    public DoubleList<FriendRequest> getSentFriendRequests() {
        return sentFriendRequests;
    }

    public DoubleList<FriendRequest> getReceivedFriendRequests() {
        return receivedFriendRequests;
    }

    // Getters y setters para listas se añadirán después

    @Override
    public String toString() {
        return "User{" +
                "persona=" + super.toString() +
                ", username='" + username + '\'' +
                // No mostrar password en toString por seguridad
                ", tipoUsuario=" + tipoUsuario +
                ", historialPrestamosCount=" + (historialPrestamos != null ? historialPrestamos.size() : 0) + // Mostrar conteo
                ", librosValoradosCount=" + (librosValorados != null ? librosValorados.size() : 0) + // Mostrar conteo
                ", sentFriendRequestsCount=" + (sentFriendRequests != null ? sentFriendRequests.size() : 0) + // Conteo
                ", receivedFriendRequestsCount=" + (receivedFriendRequests != null ? receivedFriendRequests.size() : 0) + // Conteo
                '}';
    }

    @Override
    public int compareTo(User other) {
        return this.username.compareTo(other.username);
    }

    // equals y hashCode deberían basarse en username o idPersona para consistencia
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return username.hashCode();
    }

    // Método para asegurar que las listas de FriendRequest estén inicializadas
    public void initializeFriendRequestLists() {
        if (this.sentFriendRequests == null) {
            this.sentFriendRequests = new DoubleList<>();
        }
        if (this.receivedFriendRequests == null) {
            this.receivedFriendRequests = new DoubleList<>();
        }
    }
}
