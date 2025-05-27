package org.example;

import org.example.model.Book;
import org.example.model.Loan;
import org.example.model.LoanRequest;
import org.example.model.User;
import org.example.model.enums.TipoUsuario;
import org.example.structures.doubleList.DoubleList;
import org.example.structures.colaPrioridad.ColaPrioridad;
import org.example.structures.graph.Graph; // IMPORTACIÓN AÑADIDA
import org.example.model.ChatMessage; // Importación añadida
import org.example.model.FriendRequest; // Importar FriendRequest
import org.example.model.enums.FriendRequestStatus; // Importar FriendRequestStatus

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map; // Para estadísticas

public class DigitalLibraryView extends JFrame {

    private static final String DATA_FILE_PATH = "library_data.dat";
    private DigitalLibrary biblioteca;
    private User currentUserLoggedIn = null; // Usuario actualmente logueado

    // Componentes comunes
    private JTextArea logTextArea;
    private JPanel mainContentPanel; // Panel principal que contendrá todo
    private JTabbedPane tabbedPane; // Se inicializará después del login
    private JLabel welcomeLabel;
    private JButton logoutButton;

    // Componentes Pestaña Libros
    private JTable bookCatalogTable;
    private DefaultTableModel bookCatalogTableModel;
    private JButton addBookButton;
    private JButton loadBooksButton;
    private JTextField bookTitleField, bookAuthorField, bookCategoryField, bookYearField, bookIdField, bookStockField;
    private JTextField searchBookField;
    private JButton searchBookByIdButton, searchBookByTitleButton;
    private JTextArea searchBookResultArea;

    // Componentes Pestaña Usuarios
    private JTextArea usersTextArea;
    private JButton loadUsersButton;
    private JTextField userIdField, userNameField, userLastNameField, userEmailField, userUsernameField, userPasswordField;
    private JComboBox<TipoUsuario> userTypeComboBox;
    private JButton registerUserButton;
    private JTextField searchUserField;
    private JButton searchUserByUsernameButton;
    private JTextArea searchUserResultArea;

    // Pestaña Operaciones
    private JTextField opUsernameField; // Para Username en operaciones, podría pre-llenarse
    private JTextField opBookIdLoanField, opBookIdReturnField, opBookIdRateField;
    private JSpinner opRatingSpinner; // Para la puntuación de 1-5
    private JTextField opRatingCommentField; // NUEVO: Campo para comentario de la valoración
    private JButton requestLoanButton, returnLoanButton, rateBookButton;
    private JTable activeLoansTable, waitlistTable;
    private DefaultTableModel activeLoansTableModel, waitlistTableModel;
    private JButton loadUserLoansButton, loadGlobalWaitlistButton;

    // NUEVO: Componentes para Pestaña de Recomendaciones
    private JTable recommendationsTable;
    private DefaultTableModel recommendationsTableModel;
    private JButton loadRecommendationsButton;

    // Componentes para Admin Books Panel
    private JTable adminBooksTable;
    private DefaultTableModel adminBooksTableModel;
    private JButton adminMarkBookAvailableButton;
    // private JTextField adminSearchBookField; // Comentado, decidir si se necesita específicamente aquí
    // private JButton adminSearchBookButton; // Comentado

    // Componentes para Admin Users Panel
    private JTable adminUsersTable;
    private DefaultTableModel adminUsersTableModel;
    private JButton adminDeleteUserButton;
    private JButton adminRegisterUserButton;

    // Componentes para la Pestaña de Chat
    private JComboBox<String> chatRoomComboBox;
    private JTextArea chatDisplayArea;
    private JTextField chatInputField;
    private JButton sendChatMessageButton;
    // private JButton refreshChatButton; // Eliminado

    // Componentes para la nueva pestaña "Social" (Solo LECTOR)
    private JPanel socialTabMainPanel;
    private JTable friendSuggestionsTable, pendingRequestsTable, friendsTable, sentRequestsTable; // Añadida sentRequestsTable
    private DefaultTableModel friendSuggestionsTableModel, pendingRequestsTableModel, friendsTableModel, sentRequestsTableModel;
    private JButton refreshSuggestionsButton, sendFriendRequestButton;
    private JButton acceptRequestButton, rejectRequestButton;
    private JButton removeFriendButton;
    // private JButton viewSentRequestsButton; // Botón para ver solicitudes enviadas (si se decide no usarlo, se puede quitar)

    // NUEVO: Botón para ver información del usuario sugerido
    private JButton viewSuggInfoButton;

    // NUEVO: Componentes para búsqueda directa de amigos
    private JTextField directFriendRequestUsernameField;
    private JButton directSendFriendRequestButton;

    private static final DateTimeFormatter TABLE_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public DigitalLibraryView(DigitalLibrary biblioteca) {
        this.biblioteca = biblioteca;
        setTitle("Biblioteca Digital Interactiva");
        setSize(950, 750); // Ligeramente más grande
        setLocationRelativeTo(null);
        
        // Añadir WindowListener para guardar al cerrar
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Para controlar el cierre manualmente
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Guardar datos antes de salir
                if (DigitalLibraryView.this.biblioteca != null) {
                    System.out.println("Guardando datos desde la GUI antes de salir...");
                    DigitalLibraryView.this.biblioteca.saveData(DATA_FILE_PATH);
                }
                // Proceder a cerrar la aplicación
                System.exit(0);
            }
        });
        
        // Prepara el panel principal, pero no lo llena hasta el login
        mainContentPanel = new JPanel(new BorderLayout(10, 10));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(mainContentPanel);

        // Iniciar el flujo con el diálogo de login
        // Esto debe hacerse después de que el JFrame sea visible o al menos esté listo
        SwingUtilities.invokeLater(this::showLoginDialog);
    }

    private void showLoginDialog() {
        JDialog loginDialog = new JDialog(this, "Login de Usuario", true); 
        loginDialog.setLayout(new BorderLayout(10,10)); // Usar BorderLayout para el diálogo principal
        loginDialog.setSize(400, 220); // Ligeramente más ancho y alto para asegurar espacio
        loginDialog.setLocationRelativeTo(this);
        loginDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE); 

        // Panel para los campos de texto (username, password)
        JPanel fieldsOnlyPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel userLabel = new JLabel("Username:");
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.1; fieldsOnlyPanel.add(userLabel, gbc);
        JTextField usernameField = new JTextField(20);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.9; fieldsOnlyPanel.add(usernameField, gbc);

        JLabel passLabel = new JLabel("Password:");
        gbc.gridx = 0; gbc.gridy = 1; fieldsOnlyPanel.add(passLabel, gbc);
        JPasswordField passwordField = new JPasswordField(20);
        gbc.gridx = 1; gbc.gridy = 1; fieldsOnlyPanel.add(passwordField, gbc);

        fieldsOnlyPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        loginDialog.add(fieldsOnlyPanel, BorderLayout.CENTER); // Campos en el centro

        // Panel para los botones, irá al SUR
        JPanel buttonsPanelDialog = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // Espaciado entre botones
        JButton loginButtonDialog = new JButton("Login");
        JButton cancelButtonDialog = new JButton("Salir de la Aplicación");
        buttonsPanelDialog.add(cancelButtonDialog);
        buttonsPanelDialog.add(loginButtonDialog);
        
        loginDialog.add(buttonsPanelDialog, BorderLayout.SOUTH); // Botones abajo

        // Action Listeners (sin cambios en su lógica interna)
        loginButtonDialog.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            
            User loggedUser = biblioteca.login(username, password);
            if (loggedUser != null) {
                this.currentUserLoggedIn = loggedUser;
                loginDialog.dispose(); 
                initializeMainUI();    
            } else {
                JOptionPane.showMessageDialog(loginDialog, "Username o password incorrecto.", "Error de Login", JOptionPane.ERROR_MESSAGE);
                passwordField.setText(""); 
                usernameField.selectAll();
                usernameField.requestFocus();
            }
        });
        
        cancelButtonDialog.addActionListener(e -> {
             System.exit(0);
        });

        loginDialog.getRootPane().setDefaultButton(loginButtonDialog); 
        loginDialog.setVisible(true); 
    }
    
    private void initializeMainUI() {
        mainContentPanel.removeAll(); // Limpiar el panel principal (si algo estaba antes)

        // Panel de bienvenida y logout
        JPanel topPanel = new JPanel(new BorderLayout());
        welcomeLabel = new JLabel("Bienvenido, " + currentUserLoggedIn.getUsername() + " (" + currentUserLoggedIn.getTipoUsuario().toString() + ")", SwingConstants.CENTER);
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(e -> logout());
        topPanel.add(welcomeLabel, BorderLayout.CENTER);
        topPanel.add(logoutButton, BorderLayout.EAST);
        mainContentPanel.add(topPanel, BorderLayout.NORTH);

        // Crear y añadir el JTabbedPane
        tabbedPane = new JTabbedPane();
        
        // --- Pestaña 1: Gestión de Libros (Visible para todos los usuarios logueados) ---
        tabbedPane.addTab("Gestión de Libros", createBooksPanel());

        // --- Pestaña "Gestión de Usuarios (Admin)" (Visible solo para Administradores, como pestaña principal) ---
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            tabbedPane.addTab("Gestión de Usuarios", createAdminUsersPanel());
        }
        
        // --- Pestaña: Operaciones (Visible solo para Lectores) ---
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR) {
             tabbedPane.addTab("Operaciones", createOperationsPanel());
        }

        // --- Pestaña Social (Visible para Lectores) ---
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR) {
            tabbedPane.addTab("Social", initializeSocialTab()); 
        }
        
        // --- Pestaña: Panel de Administrador (Solo para Admin)
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            JPanel adminPanel = createAdminPanel(); 
            tabbedPane.addTab("Panel Administrador", adminPanel);
        }

        // --- Pestaña de Recomendaciones (Visible para Lectores) ---
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR) {
            tabbedPane.addTab("Mis Recomendaciones", createRecommendationsPanel());
        }
        
        // --- Pestaña de Chat (Visible para Lectores y Administradores) ---
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR || currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            tabbedPane.addTab("Chat Comunitario", createChatPanel());
        }

        mainContentPanel.add(tabbedPane, BorderLayout.CENTER);

        // --- Panel de Log (Abajo, común a todas las pestañas) ---
        JPanel logPanelContainer = new JPanel(new BorderLayout());
        logPanelContainer.setBorder(BorderFactory.createTitledBorder("Log de Eventos"));
        logTextArea = new JTextArea(10, 70);
        logTextArea.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(logTextArea);
        logPanelContainer.add(logScrollPane, BorderLayout.CENTER);
        mainContentPanel.add(logPanelContainer, BorderLayout.SOUTH);

        mainContentPanel.revalidate();
        mainContentPanel.repaint();
        
        // Cargar datos iniciales
        if (tabbedPane.getTabCount() > 0 && bookCatalogTable != null) {
             loadBooks();
        }
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR) {
            if(opUsernameField != null) { 
                opUsernameField.setText(currentUserLoggedIn.getUsername());
                opUsernameField.setEditable(false); 
            }
            loadUserLoansGUI(); 
            loadGlobalWaitlistGUI();
            if (recommendationsTable != null) { // Asegurar que la tabla existe
                 loadRecommendationsGUI();
            }
            // La llamada a refreshAllSocialTables() ya se hace dentro de initializeSocialTab() 
            // la primera vez, así que no es estrictamente necesaria aquí de nuevo,
            // a menos que queramos forzar un refresco adicional por alguna razón.
            // Por ahora, la carga inicial en initializeSocialTab es suficiente.
        }
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            loadAdminBooksTable(); 
            loadAdminUsersTable();
        }
        
        if ((currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR || currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) && chatRoomComboBox != null) {
            logMessage("Chat: Intentando cargar nombres de salas..."); 
            DoubleList<String> roomNames = biblioteca.getChatRoomNames();
            
            if (roomNames != null) {
                System.out.println("[VIEW DEBUG] Salas recibidas de biblioteca.getChatRoomNames(): " + roomNames.size());
                for (int i = 0; i < roomNames.size(); i++) {
                    System.out.println("  [VIEW DEBUG] Sala: " + roomNames.get(i));
                }
            } else {
                System.out.println("[VIEW DEBUG] biblioteca.getChatRoomNames() devolvió null.");
            }

            chatRoomComboBox.removeAllItems(); 

            if (roomNames != null && !roomNames.isEmpty()) {
                logMessage("Chat: Poblando ComboBox con " + roomNames.size() + " salas.");
                for (int i = 0; i < roomNames.size(); i++) {
                    chatRoomComboBox.addItem(roomNames.get(i));
                }
                if (chatRoomComboBox.getItemCount() > 0) {
                    chatRoomComboBox.setSelectedIndex(0); 
                }
            } else {
                logMessage("Chat: No hay salas de chat para mostrar. Añadiendo 'General' por defecto.");
                chatRoomComboBox.addItem("General"); 
                if (chatRoomComboBox.getItemCount() > 0) {
                    chatRoomComboBox.setSelectedIndex(0);
                }
            }
        }
    }
    
    private void logout() {
        currentUserLoggedIn = null;
        mainContentPanel.removeAll(); // Limpiar la UI principal
        
        // Ya no mostramos el mensaje de "Sesión cerrada, reinicie".
        // JLabel loggedOutLabel = new JLabel("Sesión cerrada. Por favor, reinicie la aplicación para volver a loguear.", SwingConstants.CENTER);
        // mainContentPanel.add(loggedOutLabel, BorderLayout.CENTER);

        // En lugar de solo repintar, volvemos a mostrar el diálogo de login.
        // Si el logoutButton era parte del topPanel que se elimina con removeAll(),
        // no hay necesidad de deshabilitarlo explícitamente aquí, ya que initializeMainUI()
        // lo recreará si el login es exitoso.
        // if(logoutButton != null) logoutButton.setEnabled(false); 

        mainContentPanel.revalidate();
        mainContentPanel.repaint();

        // Llamar al diálogo de login de nuevo.
        // Esto bloqueará hasta que el diálogo se cierre (login exitoso o cancelación).
        SwingUtilities.invokeLater(this::showLoginDialog); 
    }

    // --- Creación de Panel de Libros ---
    private JPanel createBooksPanel() {
        JPanel booksTabPanel = new JPanel(new BorderLayout(10, 10));
        booksTabPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        // Panel Izquierdo: Catálogo (Visible para todos los logueados en esta pestaña)
        // Y Añadir Libro (Visible solo para Admin)
        JPanel leftBookPanel = new JPanel(new BorderLayout(5,5));
        
        JPanel displayBooksPanel = new JPanel(new BorderLayout());
        displayBooksPanel.setBorder(BorderFactory.createTitledBorder("Catálogo de Libros"));
        
        // Configuración del JTable para el catálogo de libros
        String[] columnNames = {"ID", "Título", "Autor", "Categoría", "Año Pub.", "Stock Total", "Disp."};
        bookCatalogTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Hacer la tabla no editable
            }
        };
        bookCatalogTable = new JTable(bookCatalogTableModel);
        bookCatalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookCatalogTable.setAutoCreateRowSorter(true); // Permitir ordenamiento por columnas
        // Ajustar ancho de columnas (opcional, pero útil)
        bookCatalogTable.getColumnModel().getColumn(0).setPreferredWidth(60); // ID
        bookCatalogTable.getColumnModel().getColumn(1).setPreferredWidth(200); // Título
        bookCatalogTable.getColumnModel().getColumn(2).setPreferredWidth(150); // Autor
        bookCatalogTable.getColumnModel().getColumn(3).setPreferredWidth(120); // Categoría
        bookCatalogTable.getColumnModel().getColumn(4).setPreferredWidth(60);  // Año
        bookCatalogTable.getColumnModel().getColumn(5).setPreferredWidth(80);  // Stock Total
        bookCatalogTable.getColumnModel().getColumn(6).setPreferredWidth(50);  // Disp.

        JScrollPane booksScrollPane = new JScrollPane(bookCatalogTable);
        displayBooksPanel.add(booksScrollPane, BorderLayout.CENTER);
        
        loadBooksButton = new JButton("Cargar/Actualizar Catálogo");
        loadBooksButton.addActionListener(e -> loadBooks()); // Asegurar que la acción llama a loadBooks
        displayBooksPanel.add(loadBooksButton, BorderLayout.SOUTH);
        leftBookPanel.add(displayBooksPanel, BorderLayout.CENTER);

        if (currentUserLoggedIn != null && currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            // El panel para añadir libro directamente en la pestaña se elimina.
            // Se usará un diálogo modal en su lugar.
            // JPanel addBookPanel = new JPanel(new GridBagLayout());
            // ... (todo el código del addBookPanel anterior)
            // leftBookPanel.add(addBookPanel, BorderLayout.SOUTH);
            
            // En su lugar, podemos poner el botón que abre el diálogo aquí si se desea
            // o confiar en el botón del panel de admin.
            // Por consistencia, si el admin está en esta pestaña, también debería poder añadir.
            JPanel adminActionsOnGeneralBookTabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            JButton addBookFromGeneralTabButton = new JButton("Añadir Nuevo Libro (Diálogo)");
            addBookFromGeneralTabButton.addActionListener(e -> showAddBookDialog());
            adminActionsOnGeneralBookTabPanel.add(addBookFromGeneralTabButton);
            leftBookPanel.add(adminActionsOnGeneralBookTabPanel, BorderLayout.SOUTH);
        }

        booksTabPanel.add(leftBookPanel, BorderLayout.WEST);

        // SubPanel Derecho: Buscar Libro (Visible para todos los logueados en esta pestaña)
        JPanel searchBookOuterPanel = new JPanel(new BorderLayout(5,5));
        searchBookOuterPanel.setBorder(BorderFactory.createTitledBorder("Buscar Libro"));
        JPanel searchBookInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchBookField = new JTextField(20);
        searchBookByIdButton = new JButton("Buscar por ID");
        searchBookByTitleButton = new JButton("Buscar por Título");
        searchBookInputPanel.add(new JLabel("ID/Título:"));
        searchBookInputPanel.add(searchBookField);
        searchBookInputPanel.add(searchBookByIdButton);
        searchBookInputPanel.add(searchBookByTitleButton);
        searchBookOuterPanel.add(searchBookInputPanel, BorderLayout.NORTH);
        searchBookResultArea = new JTextArea(8, 35);
        searchBookResultArea.setEditable(false);
        searchBookOuterPanel.add(new JScrollPane(searchBookResultArea), BorderLayout.CENTER);
        booksTabPanel.add(searchBookOuterPanel, BorderLayout.CENTER);

        // Listeners para botones siempre visibles en esta pestaña
        loadBooksButton.addActionListener(e -> loadBooks());
        searchBookByIdButton.addActionListener(e -> searchBookById());
        searchBookByTitleButton.addActionListener(e -> searchBookByTitle());
        
        return booksTabPanel;
    }

    // Helper para crear un panel con etiqueta y campo
    private JPanel createLabeledField(String labelText, JComponent component) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.add(new JLabel(labelText));
        panel.add(component);
        return panel;
    }

    // --- Creación de Panel de Usuarios ---
    private JPanel createUsersPanel() {
        JPanel usersTabPanel = new JPanel(new BorderLayout(10, 10));
        usersTabPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        // SubPanel Izquierdo: Lista de usuarios y Registrar Usuario
        JPanel leftUserPanel = new JPanel(new BorderLayout(5,5));

        JPanel displayUsersPanel = new JPanel(new BorderLayout());
        displayUsersPanel.setBorder(BorderFactory.createTitledBorder("Usuarios Registrados"));
        usersTextArea = new JTextArea(15, 35);
        usersTextArea.setEditable(false);
        JScrollPane usersScrollPane = new JScrollPane(usersTextArea);
        displayUsersPanel.add(usersScrollPane, BorderLayout.CENTER);
        loadUsersButton = new JButton("Cargar/Actualizar Usuarios");
        displayUsersPanel.add(loadUsersButton, BorderLayout.SOUTH);
        leftUserPanel.add(displayUsersPanel, BorderLayout.CENTER);

        JPanel registerUserPanel = new JPanel();
        registerUserPanel.setLayout(new BoxLayout(registerUserPanel, BoxLayout.Y_AXIS));
        registerUserPanel.setBorder(BorderFactory.createTitledBorder("Registrar Nuevo Usuario"));
        userIdField = new JTextField(20);
        userNameField = new JTextField(20);
        userLastNameField = new JTextField(20);
        userEmailField = new JTextField(20);
        userUsernameField = new JTextField(20);
        userPasswordField = new JTextField(20); // Temporalmente JTextField para simplicidad
        userTypeComboBox = new JComboBox<>(TipoUsuario.values());
        registerUserButton = new JButton("Registrar Usuario");
        registerUserPanel.add(createLabeledField("ID Persona:", userIdField));
        registerUserPanel.add(createLabeledField("Nombre:", userNameField));
        registerUserPanel.add(createLabeledField("Apellido:", userLastNameField));
        registerUserPanel.add(createLabeledField("Email:", userEmailField));
        registerUserPanel.add(createLabeledField("Username:", userUsernameField));
        registerUserPanel.add(createLabeledField("Password:", userPasswordField));
        registerUserPanel.add(createLabeledField("Tipo Usuario:", userTypeComboBox));
        registerUserPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        registerUserPanel.add(registerUserButton);
        leftUserPanel.add(registerUserPanel, BorderLayout.SOUTH);

        usersTabPanel.add(leftUserPanel, BorderLayout.WEST);

        // SubPanel Derecho: Buscar Usuario
        JPanel searchUserOuterPanel = new JPanel(new BorderLayout(5,5));
        searchUserOuterPanel.setBorder(BorderFactory.createTitledBorder("Buscar Usuario"));
        JPanel searchUserInputPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchUserField = new JTextField(20);
        searchUserByUsernameButton = new JButton("Buscar por Username");
        searchUserInputPanel.add(new JLabel("Username:"));
        searchUserInputPanel.add(searchUserField);
        searchUserInputPanel.add(searchUserByUsernameButton);
        searchUserOuterPanel.add(searchUserInputPanel, BorderLayout.NORTH);
        searchUserResultArea = new JTextArea(8, 35);
        searchUserResultArea.setEditable(false);
        searchUserOuterPanel.add(new JScrollPane(searchUserResultArea), BorderLayout.CENTER);
        usersTabPanel.add(searchUserOuterPanel, BorderLayout.CENTER);

        // Action Listeners para Usuarios
        loadUsersButton.addActionListener(e -> loadUsers());
        registerUserButton.addActionListener(e -> registerUser());
        searchUserByUsernameButton.addActionListener(e -> searchUserByUsername());

        return usersTabPanel;
    }

    // --- Creación de Panel de Operaciones ---
    private JPanel createOperationsPanel() {
        JPanel operationsTabPanel = new JPanel(new GridLayout(2,1, 10, 10)); // 2 Filas: Acciones y Visualizaciones
        operationsTabPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        // --- Panel Superior: Formularios de Acción ---
        JPanel topSectionPanel = new JPanel(new BorderLayout());
        JPanel commonOpPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        commonOpPanel.add(new JLabel("Username para Operación:"));
        opUsernameField = new JTextField(15);
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR) {
            opUsernameField.setText(currentUserLoggedIn.getUsername());
            opUsernameField.setEditable(false);
        }
        commonOpPanel.add(opUsernameField);
        topSectionPanel.add(commonOpPanel, BorderLayout.NORTH);

        JPanel actionsPanel = new JPanel(new GridLayout(1,3,10,10)); // 3 Columnas: Préstamo, Devolución, Valoración

        // 1. Panel Solicitar Préstamo
        JPanel requestLoanPanel = new JPanel();
        requestLoanPanel.setLayout(new BoxLayout(requestLoanPanel, BoxLayout.Y_AXIS));
        requestLoanPanel.setBorder(BorderFactory.createTitledBorder("Solicitar Préstamo"));
        opBookIdLoanField = new JTextField(15);
        requestLoanButton = new JButton("Solicitar");
        requestLoanPanel.add(createLabeledField("ID Libro:", opBookIdLoanField));
        requestLoanPanel.add(requestLoanButton);
        actionsPanel.add(requestLoanPanel);

        // 2. Panel Devolver Libro
        JPanel returnLoanPanel = new JPanel();
        returnLoanPanel.setLayout(new BoxLayout(returnLoanPanel, BoxLayout.Y_AXIS));
        returnLoanPanel.setBorder(BorderFactory.createTitledBorder("Devolver Libro"));
        opBookIdReturnField = new JTextField(15);
        returnLoanButton = new JButton("Devolver");
        returnLoanPanel.add(createLabeledField("ID Libro:", opBookIdReturnField));
        returnLoanPanel.add(returnLoanButton);
        actionsPanel.add(returnLoanPanel);

        // 3. Panel Valorar Libro
        JPanel rateBookPanel = new JPanel();
        rateBookPanel.setLayout(new BoxLayout(rateBookPanel, BoxLayout.Y_AXIS));
        rateBookPanel.setBorder(BorderFactory.createTitledBorder("Valorar Libro"));
        opBookIdRateField = new JTextField(15);
        opRatingSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1)); // Valor inicial 3, min 1, max 5, step 1
        opRatingCommentField = new JTextField(15); // NUEVO: Campo para comentario
        rateBookButton = new JButton("Valorar");
        rateBookPanel.add(createLabeledField("ID Libro:", opBookIdRateField));
        rateBookPanel.add(createLabeledField("Puntuación (1-5):", opRatingSpinner));
        rateBookPanel.add(createLabeledField("Comentario:", opRatingCommentField));
        rateBookPanel.add(rateBookButton);
        actionsPanel.add(rateBookPanel);
        
        topSectionPanel.add(actionsPanel, BorderLayout.CENTER);
        operationsTabPanel.add(topSectionPanel);

        // --- Panel Inferior: Visualización de Préstamos y Lista de Espera ---
        JPanel displayDataPanel = new JPanel(new GridLayout(1,2,10,10)); // 2 Columnas

        // Préstamos Activos del Usuario con JTable
        JPanel activeLoansPanel = new JPanel(new BorderLayout(5,5));
        activeLoansPanel.setBorder(BorderFactory.createTitledBorder("Mis Préstamos Activos"));
        String[] activeLoansColumns = {"ID Libro", "Título Libro", "Usuario", "Fecha Préstamo", "Devolución Prevista"};
        activeLoansTableModel = new DefaultTableModel(activeLoansColumns, 0) { // 0 filas iniciales
            @Override public boolean isCellEditable(int row, int column) { return false; } // No editable
        };
        activeLoansTable = new JTable(activeLoansTableModel);
        activeLoansTable.setFillsViewportHeight(true);
        JScrollPane activeLoansScrollPane = new JScrollPane(activeLoansTable);
        loadUserLoansButton = new JButton("Actualizar Mis Préstamos");
        activeLoansPanel.add(activeLoansScrollPane, BorderLayout.CENTER);
        activeLoansPanel.add(loadUserLoansButton, BorderLayout.SOUTH);
        displayDataPanel.add(activeLoansPanel);

        // Lista de Espera Global con JTable
        JPanel waitlistPanel = new JPanel(new BorderLayout(5,5));
        waitlistPanel.setBorder(BorderFactory.createTitledBorder("Lista de Espera (Global)"));
        String[] waitlistColumns = {"ID Libro", "Título Libro", "Usuario Solicitante", "Fecha Solicitud"};
        waitlistTableModel = new DefaultTableModel(waitlistColumns, 0) {
             @Override public boolean isCellEditable(int row, int column) { return false; } // No editable
        };
        waitlistTable = new JTable(waitlistTableModel);
        waitlistTable.setFillsViewportHeight(true);
        JScrollPane waitlistScrollPane = new JScrollPane(waitlistTable);
        loadGlobalWaitlistButton = new JButton("Actualizar Lista de Espera");
        waitlistPanel.add(waitlistScrollPane, BorderLayout.CENTER);
        waitlistPanel.add(loadGlobalWaitlistButton, BorderLayout.SOUTH);
        displayDataPanel.add(waitlistPanel);

        operationsTabPanel.add(displayDataPanel);

        // Action Listeners para Operaciones
        requestLoanButton.addActionListener(e -> requestLoanGUI());
        returnLoanButton.addActionListener(e -> returnLoanGUI());
        rateBookButton.addActionListener(e -> rateBookGUI());
        loadUserLoansButton.addActionListener(e -> loadUserLoansGUI());
        loadGlobalWaitlistButton.addActionListener(e -> loadGlobalWaitlistGUI());

        return operationsTabPanel;
    }

    // --- Creación de Panel de Recomendaciones ---
    private JPanel createRecommendationsPanel() {
        JPanel recommendationsPanel = new JPanel(new BorderLayout(10,10));
        recommendationsPanel.setBorder(BorderFactory.createTitledBorder("Libros Recomendados Para Ti"));

        String[] columnNames = {"Título", "Autor", "Categoría", "Año Pub.", "Calificación Prom." };
        recommendationsTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No editable
            }
        };
        recommendationsTable = new JTable(recommendationsTableModel);
        recommendationsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        recommendationsTable.setAutoCreateRowSorter(true);
        // Ajustar anchos de columna
        recommendationsTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Título
        recommendationsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Autor
        recommendationsTable.getColumnModel().getColumn(2).setPreferredWidth(120); // Categoría
        recommendationsTable.getColumnModel().getColumn(3).setPreferredWidth(60);  // Año
        recommendationsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Calificación Prom.

        JScrollPane scrollPane = new JScrollPane(recommendationsTable);
        recommendationsPanel.add(scrollPane, BorderLayout.CENTER);

        loadRecommendationsButton = new JButton("Actualizar Mis Recomendaciones");
        loadRecommendationsButton.addActionListener(e -> loadRecommendationsGUI());
        recommendationsPanel.add(loadRecommendationsButton, BorderLayout.SOUTH);

        return recommendationsPanel;
    }

    // --- Métodos de Lógica para Operaciones ---
    private String getUsernameForOperation() {
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR) {
            return currentUserLoggedIn.getUsername();
        } else {
            // Asegurarse que opUsernameField no es null si es Admin y lo usa
            return opUsernameField != null ? opUsernameField.getText() : ""; 
        }
    }

    private void requestLoanGUI() {
        String username = getUsernameForOperation();
        String bookId = opBookIdLoanField.getText();
        if (username.isEmpty() || bookId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username e ID de Libro son requeridos para solicitar préstamo.", "Error en Solicitud", JOptionPane.ERROR_MESSAGE);
            logMessage("Operaciones: Falló la validación previa para solicitar préstamo.");
            return;
        }
        // Aquí también se podrían añadir validaciones previas como en returnLoanGUI si se desea
        biblioteca.requestLoan(username, bookId);
        logMessage("Intento de solicitud de préstamo para user: "+username+", libro: "+bookId+" (GUI)");
        opBookIdLoanField.setText("");
        loadUserLoansGUI(); 
        loadGlobalWaitlistGUI();
    }

    private void returnLoanGUI() {
        String username = getUsernameForOperation();
        String bookId = opBookIdReturnField.getText();

        if (username.isEmpty() || bookId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username e ID de Libro son requeridos para devolver.", "Error en Devolución", JOptionPane.ERROR_MESSAGE);
            logMessage("Operaciones: Falló la validación previa para devolver (campos vacíos).");
            return;
        }

        User user = biblioteca.findUserByUsername(username);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Usuario '" + username + "' no encontrado.", "Error en Devolución", JOptionPane.ERROR_MESSAGE);
            logMessage("Operaciones: Falló devolución, usuario '"+username+"' no existe.");
            return;
        }

        Book book = biblioteca.findBookById(bookId);
        if (book == null) {
            JOptionPane.showMessageDialog(this, "Libro con ID '" + bookId + "' no encontrado.", "Error en Devolución", JOptionPane.ERROR_MESSAGE);
            logMessage("Operaciones: Falló devolución, libro con ID '"+bookId+"' no existe.");
            return;
        }

        // Comprobación clave: ¿Tiene el usuario este libro en préstamo activo?
        boolean hasActiveLoan = false;
        DoubleList<Loan> prestamosActivos = biblioteca.getPrestamosActivos();
        for (int i = 0; i < prestamosActivos.size(); i++) {
            Loan currentLoan = prestamosActivos.get(i);
            if (currentLoan.getLibro().getId().equals(bookId) && 
                currentLoan.getUsuario().getUsername().equals(username) && 
                !currentLoan.isDevuelto()) {
                hasActiveLoan = true;
                break;
            }
        }

        if (!hasActiveLoan) {
            JOptionPane.showMessageDialog(this, 
                "El libro '" + book.getTitulo() + "' (ID: " + bookId + ") no está actualmente en préstamo por el usuario '" + username + "'.", 
                "Error en Devolución", 
                JOptionPane.ERROR_MESSAGE);
            logMessage("Operaciones: Falló devolución, el usuario '"+username+"' no tiene el libro '"+bookId+"' en préstamo activo.");
            return;
        }

        // Si todas las validaciones pasan, proceder con la devolución en el backend
        biblioteca.returnLoan(username, bookId); // El backend aún imprimirá su propio log
        logMessage("Procesando devolución para user: "+username+", libro: "+bookId+" (GUI) tras validaciones.");
        opBookIdReturnField.setText("");
        loadUserLoansGUI();
        loadGlobalWaitlistGUI();
    }

    private void rateBookGUI() {
        String username = getUsernameForOperation();
        String bookId = opBookIdRateField.getText();
        int rating = (Integer) opRatingSpinner.getValue();
        String comment = opRatingCommentField.getText(); 

        if (username.isEmpty() || bookId.isEmpty()) {
            logMessage("Error (Valorar Libro): Username e ID de Libro son obligatorios.");
            JOptionPane.showMessageDialog(this, "Username e ID de Libro son obligatorios.", "Error al Valorar", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        User user = biblioteca.findUserByUsername(username);
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Usuario '" + username + "' no encontrado.", "Error en Valoración", JOptionPane.ERROR_MESSAGE);
            logMessage("Operaciones: Falló valoración, usuario '"+username+"' no existe.");
            return;
        }

        Book book = biblioteca.findBookById(bookId);
        if (book == null) {
            JOptionPane.showMessageDialog(this, "Libro con ID '" + bookId + "' no encontrado.", "Error en Valoración", JOptionPane.ERROR_MESSAGE);
            logMessage("Operaciones: Falló valoración, libro con ID '"+bookId+"' no existe.");
            return;
        }

        // Validar si el usuario ha tenido el libro en préstamo (el backend ya lo hace, pero una GUI amigable podría adelantarlo)
        boolean haPrestadoElLibro = false;
        DoubleList<Loan> historialPrestamos = user.getHistorialPrestamos();
        for (int i = 0; i < historialPrestamos.size(); i++) {
            Loan prestamo = historialPrestamos.get(i);
            if (prestamo.getLibro().getId().equals(bookId)) {
                haPrestadoElLibro = true;
                break;
            }
        }

        if (!haPrestadoElLibro) {
             JOptionPane.showMessageDialog(this, 
                "El usuario '" + username + "' no puede valorar el libro '" + book.getTitulo() + "' porque no lo ha tenido en préstamo.", 
                "Error en Valoración", 
                JOptionPane.ERROR_MESSAGE);
            logMessage("Operaciones: Falló valoración, '"+username+"' no ha prestado '"+bookId+"'.");
            return;
        }

        // Si validaciones previas pasan (o si decidimos delegar toda la lógica de negocio al backend)
        String result = biblioteca.valorarLibro(username, bookId, rating, comment);
        logMessage(result);
        JOptionPane.showMessageDialog(this, result, "Resultado Valoración", JOptionPane.INFORMATION_MESSAGE);
        
        // Limpiar campos después de la operación si es exitosa o como se prefiera
        opBookIdRateField.setText("");
        opRatingSpinner.setValue(5); // Reset spinner
        opRatingCommentField.setText(""); // Limpiar campo de comentario
        // opUsernameField no se limpia si es un lector logueado
    }

    private void loadUserLoansGUI() {
        if (activeLoansTable == null || currentUserLoggedIn == null) return;
        activeLoansTableModel.setRowCount(0); // Limpiar tabla
        DoubleList<Loan> todosLosPrestamosActivos = biblioteca.getPrestamosActivos();
        boolean foundLoans = false;
        for (int i = 0; i < todosLosPrestamosActivos.size(); i++) {
            Loan loan = todosLosPrestamosActivos.get(i);
            boolean showLoan = false;
            if(currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR && loan.getUsuario().getUsername().equals(currentUserLoggedIn.getUsername())){
                showLoan = true;
            } else if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR){
                String adminSpecifiedUser = opUsernameField.getText();
                if(adminSpecifiedUser.isEmpty() || loan.getUsuario().getUsername().equals(adminSpecifiedUser)){
                    showLoan = true; 
                }
            }

            if(showLoan){
                activeLoansTableModel.addRow(new Object[]{
                    loan.getLibro().getId(),
                    loan.getLibro().getTitulo(),
                    loan.getUsuario().getUsername(),
                    loan.getFechaSolicitud(),
                    loan.getFechaDevolucionPrevista()
                });
                foundLoans = true;
            }
        }
        if (!foundLoans && activeLoansTable.getRowCount() == 0) { // Comprobar si la tabla está realmente vacía
            // Podríamos añadir una fila placeholder o dejarla vacía.
            // Por ahora, el log se encarga de notificar.
        }
        logMessage("Tabla de préstamos activos actualizada.");
    }

    private void loadGlobalWaitlistGUI() {
        if (waitlistTable == null) return;
        waitlistTableModel.setRowCount(0); 
        
        ColaPrioridad<LoanRequest> colaOriginal = biblioteca.getWaitlist();
        if (colaOriginal.isEmpty()){
            // No añadir filas si está vacía, el log lo indica
        } else {
            DoubleList<LoanRequest> copiaParaMostrar = new DoubleList<>();
            ColaPrioridad<LoanRequest> tempCola = new ColaPrioridad<>();
            // Crear una copia para iterar sin modificar la original directamente en este bucle
            // Esto es importante porque poll() modifica la cola.
            ColaPrioridad<LoanRequest> iterCola = new ColaPrioridad<>();
            // Llenar tempCola y iterCola con los elementos originales
            while(!colaOriginal.isEmpty()){
                LoanRequest req = colaOriginal.poll();
                tempCola.add(req);
                iterCola.add(req); // Esta se usará para iterar y mostrar
            }
            // Restaurar la cola original
            while(!tempCola.isEmpty()){
                colaOriginal.add(tempCola.poll());
            }

            // Ahora iterar sobre iterCola (que es una copia)
            while(!iterCola.isEmpty()){ // O usar el DoubleList copiaParaMostrar si se prefiere
                LoanRequest req = iterCola.poll(); // Sacar de la copia para mostrar
                 copiaParaMostrar.addLast(req); // Si queremos usar DoubleList para el bucle final
            }

            for(int i=0; i<copiaParaMostrar.size(); i++){ // Usar la copia en DoubleList
                LoanRequest req = copiaParaMostrar.get(i);
                waitlistTableModel.addRow(new Object[]{
                    req.getBook().getId(),
                    req.getBook().getTitulo(),
                    req.getUser().getUsername(),
                    // Formatear el LocalDateTime para mejor visualización
                    req.getRequestTimestamp().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) 
                });
            }
        }
        logMessage("Tabla de lista de espera global actualizada.");
    }

    // --- Métodos Lógica Libros ---
    private void loadBooks() {
        if (bookCatalogTableModel == null) { // Asegurar que el modelo de tabla existe
            logMessage("Error: El modelo de la tabla de libros no está inicializado.");
            return;
        }
        // Limpiar filas anteriores
        bookCatalogTableModel.setRowCount(0); 
        
        DoubleList<Book> books = biblioteca.getCatalogoLibros().inOrderTraversal();
        if (books != null && !books.isEmpty()) {
            for (int i = 0; i < books.size(); i++) {
                Book book = books.get(i);
                if (book != null && book.isAvailable()) {
                    bookCatalogTableModel.addRow(new Object[]{
                            book.getId(),
                            book.getTitulo(),
                            book.getAutor(),
                            book.getCategoria(),
                            book.getAnioPublicacion(),
                            book.getStockTotal(),
                            book.getEjemplaresDisponibles()
                    });
                }
            }
            logMessage("Catálogo de libros cargado/actualizado. Total: " + books.size() + " libros.");
        } else {
            logMessage("No hay libros en el catálogo o no se pudieron cargar.");
        }
    }

    private void addBook() {
        // Este método se vuelve obsoleto o necesita ser adaptado si los campos
        // bookIdField, bookTitleField, etc., ya no están directamente en la GUI principal.
        // La lógica principal se moverá a showAddBookDialog().
        JOptionPane.showMessageDialog(this, "Esta función ha sido reemplazada por el diálogo de añadir libro.", "Info", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void searchBookById(){
        String id = searchBookField.getText();
        if (id.isEmpty()){
            logMessage("Buscar Libro: Ingrese un ID.");
            searchBookResultArea.setText("");
            return;
        }
        Book book = biblioteca.findBookById(id);
        if(book != null){
            searchBookResultArea.setText(String.format("Encontrado: ID: %s\nTítulo: %s\nAutor: %s\nCategoría: %s\nAño: %d\nStock Total: %d\nDisponibles: %d",
                book.getId(), book.getTitulo(), book.getAutor(), book.getCategoria(), book.getAnioPublicacion(), 
                book.getStockTotal(), book.getEjemplaresDisponibles()));
            logMessage("Libro encontrado por ID: " + id);
        } else {
            searchBookResultArea.setText("Libro con ID '" + id + "' no encontrado.");
            logMessage("Libro con ID '" + id + "' no encontrado.");
        }
    }

    private void searchBookByTitle(){
        String title = searchBookField.getText();
        if (title.isEmpty()){
            logMessage("Buscar Libro: Ingrese un Título.");
            searchBookResultArea.setText("");
            return;
        }
        Book book = biblioteca.findBookByTitle(title);
        if(book != null){
            searchBookResultArea.setText(String.format("Encontrado: ID: %s\nTítulo: %s\nAutor: %s\nCategoría: %s\nAño: %d\nStock Total: %d\nDisponibles: %d",
                book.getId(), book.getTitulo(), book.getAutor(), book.getCategoria(), book.getAnioPublicacion(), 
                book.getStockTotal(), book.getEjemplaresDisponibles()));
            logMessage("Libro encontrado por Título: " + title);
        } else {
            searchBookResultArea.setText("Libro con título '" + title + "' no encontrado.");
            logMessage("Libro con título '" + title + "' no encontrado.");
        }
    }

    // --- Métodos Lógica Usuarios ---
    private void loadUsers(){
        usersTextArea.setText("");
        DoubleList<User> listaUsuarios = biblioteca.getUsuarios(); // Asumiendo que DigitalLibrary.getUsuarios() devuelve DoubleList<User>
        if (listaUsuarios.isEmpty()) {
            usersTextArea.append("No hay usuarios registrados.\n");
        } else {
            for (int i = 0; i < listaUsuarios.size(); i++) {
                User user = listaUsuarios.get(i);
                usersTextArea.append(String.format("Username: %s, Nombre: %s %s, Tipo: %s\n", 
                                   user.getUsername(), user.getNombre(), user.getApellido(), user.getTipoUsuario()));
            }
        }
        logMessage("Lista de usuarios cargada/actualizada.");
    }

    private void registerUser(){
        String id = userIdField.getText();
        String nombre = userNameField.getText();
        String apellido = userLastNameField.getText();
        String email = userEmailField.getText();
        String username = userUsernameField.getText();
        String password = userPasswordField.getText(); // Por ahora userPasswordField es JTextField
        TipoUsuario tipo = (TipoUsuario) userTypeComboBox.getSelectedItem();

        if (id.isEmpty() || nombre.isEmpty() || username.isEmpty() || password.isEmpty()){
            logMessage("Error (Registrar Usuario): ID, Nombre, Username y Password son obligatorios.");
            return;
        }
        
        User newUser = new User(id, nombre, apellido, email, username, password, tipo);
        biblioteca.registerUser(newUser); // DigitalLibrary.registerUser ya maneja la lógica y logs internos
        logMessage("Intento de registro para usuario '" + username + "' (GUI).");
        loadUsers(); // Recargar lista de usuarios en la GUI
        // Limpiar campos
        userIdField.setText(""); userNameField.setText(""); userLastNameField.setText("");
        userEmailField.setText(""); userUsernameField.setText(""); userPasswordField.setText(""); 
        userTypeComboBox.setSelectedIndex(0);
    }

    private void searchUserByUsername(){
        String username = searchUserField.getText();
        if(username.isEmpty()){
            logMessage("Buscar Usuario: Ingrese un username.");
            searchUserResultArea.setText("");
            return;
        }
        User user = biblioteca.findUserByUsername(username);
        if(user != null){
            searchUserResultArea.setText(String.format("Encontrado: Username: %s\nNombre: %s %s\nEmail: %s\nTipo: %s\nPréstamos (Historial): %d\nValoraciones: %d",
                user.getUsername(), user.getNombre(), user.getApellido(), user.getEmail(), user.getTipoUsuario(), 
                user.getHistorialPrestamos().size(), user.getLibrosValorados().size()));
            logMessage("Usuario encontrado: " + username);
        } else {
            searchUserResultArea.setText("Usuario con username '" + username + "' no encontrado.");
            logMessage("Usuario '" + username + "' no encontrado.");
        }
    }
    
    // --- Método de Lógica para Recomendaciones ---
    private void loadRecommendationsGUI() {
        if (recommendationsTableModel == null || currentUserLoggedIn == null || currentUserLoggedIn.getTipoUsuario() != TipoUsuario.LECTOR) {
            // logMessage("No se pueden cargar recomendaciones en este momento.");
            return; // Solo para lectores y si la tabla está lista
        }

        recommendationsTableModel.setRowCount(0); // Limpiar tabla

        DoubleList<Book> recommendedBooks = biblioteca.getBookRecommendations(currentUserLoggedIn.getUsername());

        if (recommendedBooks != null && !recommendedBooks.isEmpty()) {
            for (int i = 0; i < recommendedBooks.size(); i++) {
                Book book = recommendedBooks.get(i);
                if (book != null) {
                    recommendationsTableModel.addRow(new Object[]{
                            book.getTitulo(),
                            book.getAutor(),
                            book.getCategoria(),
                            book.getAnioPublicacion(),
                            String.format("%.2f", book.getCalificacionPromedio()) // Formatear calificación
                    });
                }
            }
            logMessage("Recomendaciones de libros cargadas. Total: " + recommendedBooks.size() + " libros.");
        } else {
            recommendationsTableModel.addRow(new Object[]{"No hay recomendaciones disponibles por ahora.", "", "", "", ""});
            logMessage("No hay recomendaciones de libros disponibles para " + currentUserLoggedIn.getUsername() + ".");
        }
    }

    // --- Métodos para el Panel de Administrador ---
    private JPanel createAdminPanel() {
        JPanel adminPanel = new JPanel(new BorderLayout());

        JTabbedPane adminTabbedPane = new JTabbedPane();
        adminTabbedPane.addTab("Gestión de Libros (Admin)", createAdminBooksPanel());
        adminTabbedPane.addTab("Estadísticas", createAdminStatsPanel());

        adminPanel.add(adminTabbedPane, BorderLayout.CENTER);
        return adminPanel;
    }

    private JPanel createAdminBooksPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Tabla para mostrar todos los libros
        String[] columnNames = {"ID", "Título", "Autor", "Categoría", "Año", "Stock Total", "Ej. Disponibles", "Calificación", "Disponible"};
        adminBooksTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No editable directamente
            }
        };
        adminBooksTable = new JTable(adminBooksTableModel);
        adminBooksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(adminBooksTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // Panel de Acciones para libros (marcar disponible/no disponible, añadir)
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton adminAddBookButton = new JButton("Añadir Nuevo Libro");
        adminAddBookButton.addActionListener(e -> showAddBookDialogAdmin()); // Reutilizar o crear diálogo específico
        actionsPanel.add(adminAddBookButton);

        adminMarkBookAvailableButton = new JButton("Cambiar Disponibilidad");
        adminMarkBookAvailableButton.setEnabled(false); // Habilitar al seleccionar fila
        adminMarkBookAvailableButton.addActionListener(e -> toggleBookAvailabilityAdmin());
        actionsPanel.add(adminMarkBookAvailableButton);
        
        adminBooksTable.getSelectionModel().addListSelectionListener(e -> {
            adminMarkBookAvailableButton.setEnabled(adminBooksTable.getSelectedRow() != -1);
        });

        panel.add(actionsPanel, BorderLayout.SOUTH);
        
        // TODO: Añadir búsqueda si se desea (similar a la pestaña de libros general)

        return panel;
    }
    
    private void loadAdminBooksTable() {
        if (biblioteca == null || adminBooksTableModel == null) return;
        adminBooksTableModel.setRowCount(0); // Limpiar tabla
        DoubleList<Book> allBooks = biblioteca.getCatalogoLibros().inOrderTraversal(); // Obtener TODOS los libros

        for (int i = 0; i < allBooks.size(); i++) {
            Book book = allBooks.get(i);
            if (book != null) {
                adminBooksTableModel.addRow(new Object[]{
                        book.getId(),
                        book.getTitulo(),
                        book.getAutor(),
                        book.getCategoria(),
                        book.getAnioPublicacion(),
                        book.getStockTotal(),
                        book.getEjemplaresDisponibles(),
                        String.format("%.2f", book.getCalificacionPromedio()),
                        book.isAvailable() ? "Sí" : "No"
                });
            }
        }
        logMessage("Tabla de gestión de libros (Admin) actualizada. Total: " + allBooks.size() + " libros.");
    }

    private void showAddBookDialogAdmin() {
        // Este método ahora simplemente llamará al nuevo diálogo modal común.
        showAddBookDialog(); 
    }

    private void toggleBookAvailabilityAdmin() {
        int selectedRow = adminBooksTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un libro de la tabla.", "Acción Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String bookId = (String) adminBooksTableModel.getValueAt(selectedRow, 0);
        Book selectedBook = null;
        // Necesitamos obtener la instancia real del libro del catálogo
        DoubleList<Book> allBooks = biblioteca.getCatalogoLibros().inOrderTraversal();
        for(int i=0; i < allBooks.size(); i++){
            Book b = allBooks.get(i);
            if(b.getId().equals(bookId)){
                selectedBook = b;
                break;
            }
        }

        if (selectedBook == null) {
            JOptionPane.showMessageDialog(this, "No se pudo encontrar el libro seleccionado en el catálogo.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedBook.isAvailable()) {
            // Intentar marcar como no disponible
            String result = biblioteca.deleteBook(bookId); // deleteBook ahora marca como no disponible
            JOptionPane.showMessageDialog(this, result, "Resultado de Operación", JOptionPane.INFORMATION_MESSAGE);
        } else {
            // Marcar como disponible
            selectedBook.setAvailable(true);
            biblioteca.saveData(DATA_FILE_PATH); // Guardar el cambio
            JOptionPane.showMessageDialog(this, "Libro '" + selectedBook.getTitulo() + "' marcado como disponible.", "Resultado de Operación", JOptionPane.INFORMATION_MESSAGE);
        }
        loadAdminBooksTable(); // Recargar tabla
        loadBooks(); // Recargar también la tabla de catálogo general por si acaso
    }

    private JPanel createAdminUsersPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // Tabla para mostrar todos los usuarios
        String[] columnNames = {"Username", "Nombre", "Apellido", "Email", "Tipo Usuario"};
        adminUsersTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // No editable directamente
            }
        };
        adminUsersTable = new JTable(adminUsersTableModel);
        adminUsersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane tableScrollPane = new JScrollPane(adminUsersTable);
        panel.add(tableScrollPane, BorderLayout.CENTER);

        // Panel de Acciones para usuarios
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        adminRegisterUserButton = new JButton("Registrar Nuevo Usuario (Admin)");
        adminRegisterUserButton.addActionListener(e -> showRegisterUserDialogAdmin()); 
        actionsPanel.add(adminRegisterUserButton);

        // Botones para editar y eliminar (se implementarán después)
        // adminEditUserButton = new JButton("Editar Usuario Seleccionado");
        // adminEditUserButton.setEnabled(false);
        // actionsPanel.add(adminEditUserButton);
        adminDeleteUserButton = new JButton("Eliminar Usuario Seleccionado");
        adminDeleteUserButton.setEnabled(false);
        adminDeleteUserButton.addActionListener(e -> deleteUserAdmin());
        actionsPanel.add(adminDeleteUserButton);
        
        adminUsersTable.getSelectionModel().addListSelectionListener(e -> {
            boolean selected = adminUsersTable.getSelectedRow() != -1;
            // if(adminEditUserButton!=null) adminEditUserButton.setEnabled(selected);
            if(adminDeleteUserButton!=null) adminDeleteUserButton.setEnabled(selected);
        });

        panel.add(actionsPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void loadAdminUsersTable() {
        if (biblioteca == null || adminUsersTableModel == null) return;
        adminUsersTableModel.setRowCount(0); // Limpiar tabla
        DoubleList<User> allUsers = biblioteca.getUsuarios();

        for (int i = 0; i < allUsers.size(); i++) {
            User user = allUsers.get(i);
            if (user != null) {
                adminUsersTableModel.addRow(new Object[]{
                        user.getUsername(),
                        user.getNombre(),
                        user.getApellido(),
                        user.getEmail(),
                        user.getTipoUsuario().toString()
                });
            }
        }
        logMessage("Tabla de gestión de usuarios (Admin) actualizada. Total: " + allUsers.size() + " usuarios.");
    }

    private void showRegisterUserDialogAdmin() {
        // Crear un diálogo modal para el registro de usuarios por parte del admin
        JDialog registerDialog = new JDialog(this, "Registrar Nuevo Usuario (Admin)", true);
        registerDialog.setLayout(new BorderLayout(10,10));
        registerDialog.setSize(450, 350);
        registerDialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField regIdField = new JTextField(20);
        JTextField regNameField = new JTextField(20);
        JTextField regLastNameField = new JTextField(20);
        JTextField regEmailField = new JTextField(20);
        JTextField regUsernameField = new JTextField(20);
        JPasswordField regPasswordField = new JPasswordField(20);
        JComboBox<TipoUsuario> regUserTypeComboBox = new JComboBox<>(TipoUsuario.values());

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("ID Persona:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(regIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Nombre:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(regNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Apellido:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(regLastNameField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(regEmailField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Username:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(regUsernameField, gbc);
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; formPanel.add(regPasswordField, gbc);
        gbc.gridx = 0; gbc.gridy = 6; formPanel.add(new JLabel("Tipo Usuario:"), gbc);
        gbc.gridx = 1; gbc.gridy = 6; formPanel.add(regUserTypeComboBox, gbc);
        
        formPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        registerDialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton confirmRegisterButton = new JButton("Registrar");
        JButton cancelRegisterButton = new JButton("Cancelar");
        buttonsPanel.add(cancelRegisterButton);
        buttonsPanel.add(confirmRegisterButton);
        registerDialog.add(buttonsPanel, BorderLayout.SOUTH);

        confirmRegisterButton.addActionListener(ev -> {
            String id = regIdField.getText().trim();
            String nombre = regNameField.getText().trim();
            String apellido = regLastNameField.getText().trim();
            String email = regEmailField.getText().trim();
            String username = regUsernameField.getText().trim();
            String password = new String(regPasswordField.getPassword());
            TipoUsuario tipo = (TipoUsuario) regUserTypeComboBox.getSelectedItem();

            if (id.isEmpty() || nombre.isEmpty() || username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(registerDialog, "ID, Nombre, Username y Password son obligatorios.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (biblioteca.findUserByUsername(username) != null) {
                 JOptionPane.showMessageDialog(registerDialog, "El username '" + username + "' ya existe.", "Error de Validación", JOptionPane.ERROR_MESSAGE);
                return;
            }

            User newUser = new User(id, nombre, apellido, email, username, password, tipo);
            biblioteca.registerUser(newUser);
            logMessage("Usuario '" + username + "' registrado por administrador.");
            loadAdminUsersTable(); // Recargar la tabla de usuarios admin
            registerDialog.dispose();
        });

        cancelRegisterButton.addActionListener(ev -> registerDialog.dispose());
        registerDialog.getRootPane().setDefaultButton(confirmRegisterButton);
        registerDialog.setVisible(true);
    }

    private void deleteUserAdmin() {
        int selectedRow = adminUsersTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario de la tabla.", "Acción Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String usernameToDelete = (String) adminUsersTableModel.getValueAt(selectedRow, 0); // Columna 0 es Username

        if (currentUserLoggedIn != null && currentUserLoggedIn.getUsername().equals(usernameToDelete)) {
            JOptionPane.showMessageDialog(this, "Un administrador no puede eliminarse a sí mismo a través de esta interfaz.", "Operación no permitida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de que desea eliminar al usuario '" + usernameToDelete + "'?\n" +
                "Esta acción también eliminará su historial de préstamos y valoraciones.", 
                "Confirmar Eliminación de Usuario", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            String result = biblioteca.deleteUser(usernameToDelete);
            JOptionPane.showMessageDialog(this, result, "Resultado de Eliminación", JOptionPane.INFORMATION_MESSAGE);
            logMessage(result); // Loguear el resultado del backend
            loadAdminUsersTable(); // Recargar la tabla de usuarios admin
            // Considerar recargar la tabla de libros si las calificaciones promedio pueden cambiar
            loadAdminBooksTable(); 
            loadBooks();
        }
    }

    private JPanel createAdminStatsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = GridBagConstraints.RELATIVE; // Para apilar verticalmente
        gbc.weightx = 1.0;

        JButton loansPerUserButton = new JButton("Ver Préstamos por Lector");
        loansPerUserButton.addActionListener(e -> showLoansPerUserStats());
        panel.add(loansPerUserButton, gbc);

        JButton mostRatedBooksButton = new JButton("Ver Libros Más Valorados");
        mostRatedBooksButton.addActionListener(e -> showMostRatedBooksStats());
        panel.add(mostRatedBooksButton, gbc);

        JButton mostConnectionsButton = new JButton("Ver Lectores con Más Conexiones");
        mostConnectionsButton.addActionListener(e -> showMostConnectionsStats());
        panel.add(mostConnectionsButton, gbc);

        // Panel para Camino Más Corto
        JPanel shortestPathPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        shortestPathPanel.setBorder(BorderFactory.createTitledBorder("Camino Más Corto entre Lectores"));
        shortestPathPanel.add(new JLabel("Usuario 1:"));
        JTextField spUser1Field = new JTextField(10);
        shortestPathPanel.add(spUser1Field);
        shortestPathPanel.add(new JLabel("Usuario 2:"));
        JTextField spUser2Field = new JTextField(10);
        shortestPathPanel.add(spUser2Field);
        JButton findShortestPathButton = new JButton("Buscar Camino");
        findShortestPathButton.addActionListener(e -> showShortestPath(spUser1Field.getText(), spUser2Field.getText()));
        shortestPathPanel.add(findShortestPathButton);
        panel.add(shortestPathPanel, gbc);

        JButton viewGraphButton = new JButton("Visualizar Grafo de Afinidad");
        viewGraphButton.addActionListener(e -> guiShowAffinityGraph());
        panel.add(viewGraphButton, gbc);

        JButton recalculateAffinitiesButton = new JButton("Recalcular Afinidades por Gustos");
        recalculateAffinitiesButton.addActionListener(e -> {
            if (biblioteca != null) {
                String result = biblioteca.adminRecalculateAllAffinities();
                JOptionPane.showMessageDialog(this, result, "Recálculo de Afinidades", JOptionPane.INFORMATION_MESSAGE);
                logMessage("Admin: " + result);
                // Opcional: Si la visualización del grafo está abierta, podría necesitar actualizarse,
                // o simplemente pedir al admin que la cierre y la vuelva a abrir.
            }
        });
        panel.add(recalculateAffinitiesButton, gbc);
        
        // Para ocupar el espacio restante y empujar todo hacia arriba
        gbc.weighty = 1.0;
        panel.add(new JPanel(), gbc); 

        return panel;
    }

    private void showLoansPerUserStats() {
        java.util.Map<String, Integer> loanCounts = biblioteca.getLoanCountsPerUser();
        
        if (loanCounts.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay datos de préstamos para mostrar.", "Estadísticas: Préstamos por Lector", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] columnNames = {"Username Lector", "Cantidad de Préstamos"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

        for (java.util.Map.Entry<String, Integer> entry : loanCounts.entrySet()) {
            tableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }

        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true); // Permitir ordenamiento
        table.setFillsViewportHeight(true);
        // Deshabilitar edición de celdas
        table.setDefaultEditor(Object.class, null);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(400, 300)); // Ajustar tamaño según necesidad

        JOptionPane.showMessageDialog(this, scrollPane, "Estadísticas: Préstamos por Lector", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showMostRatedBooksStats() {
        String countStr = JOptionPane.showInputDialog(this, "Mostrar los N libros más valorados:", "5");
        if (countStr == null) return; // Usuario canceló
        try {
            int count = Integer.parseInt(countStr);
            if (count <= 0) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un número positivo.", "Entrada Inválida", JOptionPane.ERROR_MESSAGE);
                return;
            }
            DoubleList<Book> topBooks = biblioteca.getMostRatedBooks(count);
            
            if (topBooks.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No hay libros valorados o disponibles para mostrar.", "Estadísticas: Libros Más Valorados", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] columnNames = {"Ranking", "Título", "Autor", "Categoría", "Año Pub.", "Calificación Prom." };
            DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);

            for (int i = 0; i < topBooks.size(); i++) {
                Book book = topBooks.get(i);
                tableModel.addRow(new Object[]{
                        i + 1, // Ranking
                        book.getTitulo(),
                        book.getAutor(),
                        book.getCategoria(),
                        book.getAnioPublicacion(),
                        String.format("%.2f", book.getCalificacionPromedio())
                });
            }

            JTable table = new JTable(tableModel);
            table.setAutoCreateRowSorter(true);
            table.setFillsViewportHeight(true);
            table.setDefaultEditor(Object.class, null); // No editable
            
            // Ajustar anchos de columna si se desea
            table.getColumnModel().getColumn(0).setPreferredWidth(60);  // Ranking
            table.getColumnModel().getColumn(1).setPreferredWidth(250); // Título
            table.getColumnModel().getColumn(2).setPreferredWidth(180); // Autor
            table.getColumnModel().getColumn(3).setPreferredWidth(120); // Categoría
            table.getColumnModel().getColumn(4).setPreferredWidth(70);  // Año
            table.getColumnModel().getColumn(5).setPreferredWidth(100); // Calificación

            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setPreferredSize(new Dimension(750, 350)); // Ajustar tamaño para más columnas

            JOptionPane.showMessageDialog(this, scrollPane, "Estadísticas: Libros Más Valorados (Top " + count + ")", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Entrada inválida. Por favor, ingrese un número.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showMostConnectionsStats() {
        String countStr = JOptionPane.showInputDialog(this, "Mostrar los N lectores con más conexiones:", "5");
        if (countStr == null) return; // Usuario canceló
        try {
            int count = Integer.parseInt(countStr);
             if (count <= 0) {
                JOptionPane.showMessageDialog(this, "Por favor, ingrese un número positivo.", "Entrada Inválida", JOptionPane.ERROR_MESSAGE);
                return;
            }
            java.util.Map<String, Integer> topUsers = biblioteca.getUsersWithMostConnections(count);
            StringBuilder sb = new StringBuilder("--- Lectores con Más Conexiones (Top " + count + ") ---\n");
            if (topUsers.isEmpty()) {
                sb.append("No hay datos de conexiones o usuarios.");
            } else {
                int rank = 1;
                for (java.util.Map.Entry<String, Integer> entry : topUsers.entrySet()) {
                    sb.append(String.format("%d. %s - Conexiones: %d\n", rank++, entry.getKey(), entry.getValue()));
                }
            }
            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(400, 300));
            JOptionPane.showMessageDialog(this, scrollPane, "Estadísticas: Lectores con Más Conexiones", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Entrada inválida. Por favor, ingrese un número.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void showShortestPath(String username1, String username2) {
        if (username1 == null || username1.trim().isEmpty() || username2 == null || username2.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese ambos nombres de usuario.", "Entrada Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        DoubleList<User> path = biblioteca.findShortestPathAffinity(username1.trim(), username2.trim());
        StringBuilder sb = new StringBuilder();
        if (path.isEmpty()) {
            sb.append("No se encontró un camino de afinidad entre '" + username1 + "' y '" + username2 + "'.");
        } else {
            sb.append("Camino más corto entre '" + username1 + "' y '" + username2 + "':\n");
            for (int i = 0; i < path.size(); i++) {
                sb.append(path.get(i).getUsername());
                if (i < path.size() - 1) {
                    sb.append(" -> ");
                }
            }
        }
        JOptionPane.showMessageDialog(this, sb.toString(), "Resultado: Camino de Afinidad", JOptionPane.INFORMATION_MESSAGE);
    }

    private void guiShowAffinityGraph() {
        if (biblioteca == null || biblioteca.getRedAfinidad() == null) {
            JOptionPane.showMessageDialog(this, "No hay datos del grafo para mostrar.", "Grafo Vacío", JOptionPane.INFORMATION_MESSAGE);
            logMessage("Admin: No se puede mostrar el grafo, no hay datos.");
            return;
        }

        Graph<User> affinityGraph = biblioteca.getRedAfinidad();
        if (affinityGraph.getVertices().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El grafo de afinidad está vacío (no hay usuarios o conexiones).", "Grafo Vacío", JOptionPane.INFORMATION_MESSAGE);
            logMessage("Admin: El grafo de afinidad está vacío.");
            return;
        }

        JDialog graphDialog = new JDialog(this, "Visualizador del Grafo de Afinidad", true); // true para modal
        GraphDisplayPanel displayPanel = new GraphDisplayPanel(affinityGraph);
        JScrollPane scrollPane = new JScrollPane(displayPanel); // Añadir scroll por si el grafo es grande
        
        graphDialog.add(scrollPane);
        graphDialog.setSize(650, 650); // Un poco más grande que el preferredSize del panel para acomodar bordes/scrollbars
        graphDialog.setLocationRelativeTo(this);
        graphDialog.setVisible(true);
        logMessage("Admin: Mostrando visualización del grafo de afinidad.");
    }

    // --- Método Común de Log ---
    public void logMessage(String message) {
        if (logTextArea == null) { // Inicializar si es nulo (puede pasar si se llama antes de initializeMainUI)
            logTextArea = new JTextArea(); // Esto es un fallback, debería estar inicializado
        }
        SwingUtilities.invokeLater(() -> { 
            logTextArea.append(message + "\n");
            logTextArea.setCaretPosition(logTextArea.getDocument().getLength()); 
        });
    }

    // --- Nuevo Diálogo Modal para Añadir Libros ---
    private void showAddBookDialog() {
        JDialog addBookDialog = new JDialog(this, "Añadir Nuevo Libro", true); // true para modal
        addBookDialog.setLayout(new BorderLayout(10, 10));
        addBookDialog.setSize(450, 300); // Ajustar tamaño según necesidad
        addBookDialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Campos locales para el diálogo
        JTextField dlgBookIdField = new JTextField(20);
        JTextField dlgBookTitleField = new JTextField(20);
        JTextField dlgBookAuthorField = new JTextField(20);
        JTextField dlgBookCategoryField = new JTextField(20);
        JTextField dlgBookYearField = new JTextField(20);
        JTextField dlgBookStockField = new JTextField(20);

        gbc.gridx = 0; gbc.gridy = 0; formPanel.add(new JLabel("ID:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; formPanel.add(dlgBookIdField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; formPanel.add(new JLabel("Título:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; formPanel.add(dlgBookTitleField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; formPanel.add(new JLabel("Autor:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; formPanel.add(dlgBookAuthorField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; formPanel.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; formPanel.add(dlgBookCategoryField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; formPanel.add(new JLabel("Año Pub.:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; formPanel.add(dlgBookYearField, gbc);
        gbc.gridx = 0; gbc.gridy = 5; formPanel.add(new JLabel("Stock Inicial:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; formPanel.add(dlgBookStockField, gbc);
        
        formPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        addBookDialog.add(formPanel, BorderLayout.CENTER);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Guardar Libro");
        JButton cancelButton = new JButton("Cancelar");
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(saveButton);
        addBookDialog.add(buttonsPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> {
            String id = dlgBookIdField.getText().trim();
            String title = dlgBookTitleField.getText().trim();
            String author = dlgBookAuthorField.getText().trim();
            String category = dlgBookCategoryField.getText().trim();
            String yearStr = dlgBookYearField.getText().trim();
            String stockStr = dlgBookStockField.getText().trim();

            if (id.isEmpty() || title.isEmpty() || author.isEmpty() || category.isEmpty() || yearStr.isEmpty() || stockStr.isEmpty()) {
                JOptionPane.showMessageDialog(addBookDialog, "Todos los campos son obligatorios.", "Error al Añadir Libro", JOptionPane.ERROR_MESSAGE);
                return;
            }
            // Validar si el ID del libro ya existe
            if (biblioteca.findBookById(id) != null || bookIdExistsInCatalog(id)) { // Uso del método corregido
                 JOptionPane.showMessageDialog(addBookDialog, "Error: Ya existe un libro con el ID '" + id + "'. El ID debe ser único.", "Error de ID Duplicado", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int year = Integer.parseInt(yearStr);
                int stock = Integer.parseInt(stockStr);

                if (stock < 0) {
                    JOptionPane.showMessageDialog(addBookDialog, "El stock no puede ser negativo.", "Error al Añadir Libro", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Book newBook = new Book(id, title, author, category, year, stock);
                biblioteca.addBook(newBook);
                logMessage("Libro añadido: " + title + " (ID: " + id + ", Stock: " + stock + ")");
                
                loadBooks(); // Recargar la tabla de catálogo general
                if (adminBooksTableModel != null) { // Si la tabla de admin existe, recargarla también
                    loadAdminBooksTable();
                }
                
                addBookDialog.dispose(); // Cerrar el diálogo

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addBookDialog, "El año y el stock deben ser números válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> addBookDialog.dispose());
        addBookDialog.getRootPane().setDefaultButton(saveButton);
        addBookDialog.setVisible(true);
    }
    
    // Método auxiliar para comprobar si un ID de libro ya existe en el catálogo (disponible o no)
    // Necesario para la validación en el diálogo de añadir libro.
    private boolean bookIdExistsInCatalog(String bookId) { // Nombre corregido
        DoubleList<Book> todosLosLibrosCatalogo = biblioteca.getCatalogoLibros().inOrderTraversal();
        for (int i = 0; i < todosLosLibrosCatalogo.size(); i++) {
            if (todosLosLibrosCatalogo.get(i).getId().equals(bookId)) {
                return true;
            }
        }
        return false;
    }

    // --- Creación de Panel de Chat ---
    private JPanel createChatPanel() {
        JPanel chatPanel = new JPanel(new BorderLayout(10, 10));
        chatPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel Superior: Selección de Sala y Botón de Refrescar
        JPanel topChatPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chatRoomComboBox = new JComboBox<>();
        // refreshChatButton = new JButton("Refrescar Chat"); // Eliminado
        topChatPanel.add(new JLabel("Sala de Chat:"));
        topChatPanel.add(chatRoomComboBox);
        // topChatPanel.add(refreshChatButton); // Eliminado
        chatPanel.add(topChatPanel, BorderLayout.NORTH);

        // Área Central: Visualización de Mensajes
        chatDisplayArea = new JTextArea();
        chatDisplayArea.setEditable(false);
        chatDisplayArea.setLineWrap(true);
        chatDisplayArea.setWrapStyleWord(true);
        JScrollPane chatScrollPane = new JScrollPane(chatDisplayArea);
        chatPanel.add(chatScrollPane, BorderLayout.CENTER);

        // Panel Inferior: Campo de Entrada y Botón de Enviar
        JPanel bottomChatPanel = new JPanel(new BorderLayout(5, 0));
        chatInputField = new JTextField();
        sendChatMessageButton = new JButton("Enviar");
        bottomChatPanel.add(chatInputField, BorderLayout.CENTER);
        bottomChatPanel.add(sendChatMessageButton, BorderLayout.EAST);
        chatPanel.add(bottomChatPanel, BorderLayout.SOUTH);

        // Action Listeners para el Chat
        chatRoomComboBox.addActionListener(e -> loadChatMessagesGUI());
        // refreshChatButton.addActionListener(e -> loadChatMessagesGUI()); // Eliminado
        sendChatMessageButton.addActionListener(e -> sendChatMessageGUI());
        // Permitir enviar con Enter en el campo de texto
        chatInputField.addActionListener(e -> sendChatMessageGUI()); 

        return chatPanel;
    }

    // --- Métodos de Lógica para el Chat ---
    private void loadChatMessagesGUI() {
        if (chatDisplayArea == null || chatRoomComboBox == null || biblioteca == null || currentUserLoggedIn == null) {
            // logMessage("Chat: No se pueden cargar mensajes, componentes no listos.");
            return;
        }
        String selectedRoom = (String) chatRoomComboBox.getSelectedItem();
        if (selectedRoom == null) {
            // logMessage("Chat: Ninguna sala seleccionada.");
            chatDisplayArea.setText(""); // Limpiar área si no hay sala
            return;
        }

        chatDisplayArea.setText(""); // Limpiar antes de cargar nuevos mensajes
        DoubleList<ChatMessage> messages = biblioteca.getChatMessages(selectedRoom);

        if (messages != null && !messages.isEmpty()) {
            for (int i = 0; i < messages.size(); i++) {
                ChatMessage msg = messages.get(i);
                chatDisplayArea.append(msg.getFormattedMessage() + "\n");
            }
        } else {
            chatDisplayArea.append("No hay mensajes en la sala '" + selectedRoom + "'. ¡Sé el primero en escribir!\n");
        }
        // Auto-scroll al final
        chatDisplayArea.setCaretPosition(chatDisplayArea.getDocument().getLength());
        logMessage("Chat: Mensajes cargados para la sala: " + selectedRoom);
    }

    private void sendChatMessageGUI() {
        if (chatInputField == null || chatRoomComboBox == null || biblioteca == null || currentUserLoggedIn == null) {
            logMessage("Chat: No se puede enviar mensaje, componentes no listos.");
            return;
        }
        String messageContent = chatInputField.getText().trim();
        String selectedRoom = (String) chatRoomComboBox.getSelectedItem();

        if (messageContent.isEmpty()) {
            // JOptionPane.showMessageDialog(this, "El mensaje no puede estar vacío.", "Error de Chat", JOptionPane.WARNING_MESSAGE);
            return; // No enviar mensajes vacíos
        }
        if (selectedRoom == null) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una sala de chat.", "Error de Chat", JOptionPane.ERROR_MESSAGE);
            return;
        }

        ChatMessage newMessage = new ChatMessage(currentUserLoggedIn.getUsername(), selectedRoom, messageContent);
        biblioteca.sendChatMessage(selectedRoom, newMessage); // El método sendChatMessage en DigitalLibrary ya se encarga de la lógica de añadirlo a la sala correcta.

        chatInputField.setText(""); // Limpiar campo de entrada
        loadChatMessagesGUI(); // Recargar mensajes para ver el nuevo
        logMessage("Chat: Mensaje enviado a la sala: " + selectedRoom);
    }

    // Nuevo método para inicializar la pestaña Social
    private JPanel initializeSocialTab() {
        socialTabMainPanel = new JPanel(); 
        socialTabMainPanel.setLayout(new BoxLayout(socialTabMainPanel, BoxLayout.Y_AXIS));
        socialTabMainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel superior para Sugerencias y Solicitudes Recibidas (lado a lado)
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 0));

        // 1. Panel de Sugerencias de Amigos
        JPanel suggestionsPanel = new JPanel(new BorderLayout(5, 5));
        suggestionsPanel.setBorder(BorderFactory.createTitledBorder("Sugerencias de Amigos"));
        // MODIFICADAS LAS COLUMNAS
        friendSuggestionsTableModel = new DefaultTableModel(new String[]{"Usuario Sugerido", "Origen de Sugerencia"}, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        friendSuggestionsTable = new JTable(friendSuggestionsTableModel);
        suggestionsPanel.add(new JScrollPane(friendSuggestionsTable), BorderLayout.CENTER);
        JPanel suggestionsButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewSuggInfoButton = new JButton("Ver Info"); // Crear botón
        refreshSuggestionsButton = new JButton("Actualizar Sugerencias");
        // sendFriendRequestButton = new JButton("Enviar Solicitud"); // Ya no se inicializa aquí, se elimina el botón separado
        suggestionsButtonsPanel.add(refreshSuggestionsButton);
        suggestionsButtonsPanel.add(viewSuggInfoButton); // Añadir botón
        // suggestionsButtonsPanel.add(sendFriendRequestButton); // ELIMINAR ESTA LÍNEA
        suggestionsPanel.add(suggestionsButtonsPanel, BorderLayout.SOUTH);
        topPanel.add(suggestionsPanel);

        // 2. Panel de Solicitudes de Amistad Pendientes (Recibidas)
        JPanel pendingRequestsPanel = new JPanel(new BorderLayout(5,5));
        pendingRequestsPanel.setBorder(BorderFactory.createTitledBorder("Solicitudes de Amistad Recibidas"));
        pendingRequestsTableModel = new DefaultTableModel(new Object[][]{}, new String[]{"De", "Fecha"}) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        pendingRequestsTable = new JTable(pendingRequestsTableModel);
        pendingRequestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingRequestsPanel.add(new JScrollPane(pendingRequestsTable), BorderLayout.CENTER);
        JPanel pendingRequestsButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        acceptRequestButton = new JButton("Aceptar");
        rejectRequestButton = new JButton("Rechazar");
        pendingRequestsButtonsPanel.add(acceptRequestButton);
        pendingRequestsButtonsPanel.add(rejectRequestButton);
        pendingRequestsPanel.add(pendingRequestsButtonsPanel, BorderLayout.SOUTH);
        topPanel.add(pendingRequestsPanel);

        socialTabMainPanel.add(topPanel); // Añadir directamente al BoxLayout

        // Panel INTERMEDIO para Amigos Actuales y Solicitudes Enviadas (lado a lado)
        // Este era llamado 'bottomPanel' antes, lo renombro para claridad en la estructura vertical
        JPanel middlePanel = new JPanel(new GridLayout(1, 2, 10, 10)); 
        
        JPanel friendsPanel = new JPanel(new BorderLayout(5,5));
        friendsPanel.setBorder(BorderFactory.createTitledBorder("Mis Amigos"));
        friendsTableModel = new DefaultTableModel(new Object[][]{}, new String[]{"Amigo"}) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        friendsTable = new JTable(friendsTableModel);
        friendsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        friendsPanel.add(new JScrollPane(friendsTable), BorderLayout.CENTER);
        JPanel friendsButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        removeFriendButton = new JButton("Eliminar Amigo");
        friendsButtonsPanel.add(removeFriendButton);
        friendsPanel.add(friendsButtonsPanel, BorderLayout.SOUTH);
        middlePanel.add(friendsPanel);

        JPanel sentRequestsPanel = new JPanel(new BorderLayout(5,5));
        sentRequestsPanel.setBorder(BorderFactory.createTitledBorder("Mis Solicitudes Enviadas"));
        sentRequestsTableModel = new DefaultTableModel(new Object[][]{}, new String[]{"Para", "Estado", "Fecha"}){
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        sentRequestsTable = new JTable(sentRequestsTableModel);
        sentRequestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        sentRequestsPanel.add(new JScrollPane(sentRequestsTable), BorderLayout.CENTER);
        middlePanel.add(sentRequestsPanel);

        socialTabMainPanel.add(middlePanel); // Añadir directamente al BoxLayout

        // Panel INFERIOR para búsqueda directa de amigos
        // Este era 'directSearchPanel', lo renombro para claridad
        JPanel bottomSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomSearchPanel.setBorder(BorderFactory.createTitledBorder("Buscar y Enviar Solicitud Directa"));
        bottomSearchPanel.add(new JLabel("Username del Usuario:"));
        directFriendRequestUsernameField = new JTextField(15);
        bottomSearchPanel.add(directFriendRequestUsernameField);
        directSendFriendRequestButton = new JButton("Enviar Solicitud Directa");
        bottomSearchPanel.add(directSendFriendRequestButton);
        
        // Añadir el nuevo panel al sur del panel principal de la pestaña social
        // socialTabMainPanel.add(directSearchPanel, BorderLayout.SOUTH); // Ya no se usa BorderLayout aquí
        socialTabMainPanel.add(bottomSearchPanel); // Añadir directamente al BoxLayout

        viewSuggInfoButton.addActionListener(e -> guiHandleViewSuggInfo()); // Añadir ActionListener
        refreshSuggestionsButton.addActionListener(e -> guiHandleRefreshSuggestions());
        // sendFriendRequestButton.addActionListener(e -> guiHandleSendFriendRequest()); // ELIMINAR ESTA LÍNEA, ya no hay botón
        acceptRequestButton.addActionListener(e -> guiHandleAcceptFriendRequest());
        rejectRequestButton.addActionListener(e -> guiHandleRejectFriendRequest());
        removeFriendButton.addActionListener(e -> guiHandleRemoveFriend());
        
        directSendFriendRequestButton.addActionListener(e -> guiHandleDirectSendFriendRequest());
        
        refreshAllSocialTables(); // Asegurarse de que esta línea está descomentada
        return socialTabMainPanel;
    }

    private void refreshFriendSuggestionsTable() {
        if (friendSuggestionsTableModel == null || currentUserLoggedIn == null) return;
        friendSuggestionsTableModel.setRowCount(0); // Limpiar tabla

        // LLAMAR AL NUEVO MÉTODO DE SUGERENCIAS BASADO EN RED
        DoubleList<User> suggestions = biblioteca.getFriendSuggestionsByNetwork(currentUserLoggedIn.getUsername(), 10); 

        if (suggestions != null) {
            for (int i = 0; i < suggestions.size(); i++) {
                User suggestedUser = suggestions.get(i);
                if (suggestedUser != null) {
                    // AÑADIR A LA TABLA CON EL NUEVO FORMATO
                    friendSuggestionsTableModel.addRow(new Object[]{suggestedUser.getUsername(), "Conexión de red"});
                }
            }
        } else {
            logMessage("Social: La lista de sugerencias de red devuelta es null.");
        }
        logMessage("Social: Tabla de sugerencias de amigos (por red) actualizada.");
    }

    private void refreshPendingRequestsTable() {
        if (currentUserLoggedIn == null || currentUserLoggedIn.getTipoUsuario() != TipoUsuario.LECTOR || pendingRequestsTableModel == null) return;
        pendingRequestsTableModel.setRowCount(0);
        DoubleList<FriendRequest> requests = biblioteca.getPendingFriendRequests(currentUserLoggedIn.getUsername());
        if (requests != null) {
            for (int i = 0; i < requests.size(); i++) {
                FriendRequest req = requests.get(i);
                if (req != null) {
                    pendingRequestsTableModel.addRow(new Object[]{
                        req.getSenderUsername(),
                        req.getRequestTimestamp().format(TABLE_DATE_TIME_FORMATTER) // Usar el formateador definido
                    });
                }
            }
        }
        logMessage("Tabla de solicitudes de amistad pendientes actualizada.");
    }

    private void refreshFriendsTable() {
        if (currentUserLoggedIn == null || currentUserLoggedIn.getTipoUsuario() != TipoUsuario.LECTOR || friendsTableModel == null) return;
        friendsTableModel.setRowCount(0);
        // Asegurarse de que currentUserLoggedIn está en la red de afinidad antes de pedir vecinos
        if (biblioteca.getRedAfinidad().hasVertex(currentUserLoggedIn)) {
            DoubleList<User> friends = biblioteca.getRedAfinidad().getNeighbors(currentUserLoggedIn);
            if (friends != null) {
                for (int i = 0; i < friends.size(); i++) {
                    User friend = friends.get(i);
                    if (friend != null) {
                        friendsTableModel.addRow(new Object[]{friend.getUsername()});
                    }
                }
            }
        } // Si no está en el grafo, la tabla simplemente permanecerá vacía, lo cual es correcto.
        logMessage("Tabla de amigos actualizada.");
    }

    private void refreshSentRequestsTable() {
        if (currentUserLoggedIn == null || currentUserLoggedIn.getTipoUsuario() != TipoUsuario.LECTOR || sentRequestsTableModel == null) return;
        sentRequestsTableModel.setRowCount(0);
        DoubleList<FriendRequest> requests = biblioteca.getSentFriendRequestsWithStatus(currentUserLoggedIn.getUsername());
        if (requests != null) {
            for (int i = 0; i < requests.size(); i++) {
                FriendRequest req = requests.get(i);
                if (req != null) {
                    sentRequestsTableModel.addRow(new Object[]{
                        req.getReceiverUsername(),
                        req.getStatus().toString(),
                        req.getRequestTimestamp().format(TABLE_DATE_TIME_FORMATTER) // Usar el formateador
                    });
                }
            }
        }
        logMessage("Tabla de solicitudes de amistad enviadas actualizada.");
    }

    private void refreshAllSocialTables() {
        if (currentUserLoggedIn != null && currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR) {
            refreshFriendSuggestionsTable();
            refreshPendingRequestsTable();
            refreshFriendsTable();
            refreshSentRequestsTable();
        }
    }

    // Nuevos métodos para manejar acciones de la GUI Social
    private void guiHandleRefreshSuggestions() {
        logMessage("Social: Actualizando sugerencias de amigos...");
        refreshFriendSuggestionsTable();
        // Opcionalmente, también refrescar solicitudes enviadas si la lógica de sugerencias cambia algo allí
        // refreshSentRequestsTable(); 
    }

    private void guiHandleDirectSendFriendRequest() {
        if (currentUserLoggedIn == null || directFriendRequestUsernameField == null) {
            return;
        }
        String targetUsername = directFriendRequestUsernameField.getText().trim();

        if (targetUsername.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Por favor, ingrese el nombre de usuario para buscar.", "Acción Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (targetUsername.equals(currentUserLoggedIn.getUsername())) {
            JOptionPane.showMessageDialog(this, "No puedes buscarte a ti mismo.", "Operación no permitida", JOptionPane.ERROR_MESSAGE);
            return;
        }

        User targetUser = biblioteca.findUserByUsername(targetUsername);

        if (targetUser == null) {
            JOptionPane.showMessageDialog(this, "Usuario '" + targetUsername + "' no encontrado.", "Búsqueda de Usuario", JOptionPane.INFORMATION_MESSAGE);
            directFriendRequestUsernameField.setText(""); // Limpiar campo
            return;
        }

        // Si se encuentra el usuario, mostrar su información y opción para enviar solicitud
        DoubleList<String> preferredCategories = biblioteca.getPreferredCategories(targetUser);
        StringBuilder info = new StringBuilder();
        info.append("Información del Usuario:\n");
        info.append("Username: ").append(targetUser.getUsername()).append("\n");
        info.append("Nombre: ").append(targetUser.getNombre()).append(" ").append(targetUser.getApellido()).append("\n");
        info.append("Categorías Favoritas: ");
        if (preferredCategories.isEmpty()) {
            info.append("No especificadas aún.\n\n");
        } else {
            for (int i = 0; i < preferredCategories.size(); i++) {
                info.append(preferredCategories.get(i));
                if (i < preferredCategories.size() - 1) {
                    info.append(", ");
                }
            }
            info.append("\n\n");
        }
        info.append("¿Desea enviar una solicitud de amistad a este usuario?");

        int option = JOptionPane.showConfirmDialog(this, 
                                                 info.toString(), 
                                                 "Confirmar Envío de Solicitud", 
                                                 JOptionPane.YES_NO_OPTION, 
                                                 JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            String result = biblioteca.sendFriendRequest(currentUserLoggedIn.getUsername(), targetUser.getUsername());
            JOptionPane.showMessageDialog(this, result, "Enviar Solicitud de Amistad", JOptionPane.INFORMATION_MESSAGE);
            logMessage("Social: Intento de enviar solicitud directa a '" + targetUser.getUsername() + "'. Resultado: " + result);
            refreshAllSocialTables(); // Actualizar todas las tablas sociales
        } else {
            logMessage("Social: Envío de solicitud a '" + targetUser.getUsername() + "' cancelado por el usuario.");
        }
        
        directFriendRequestUsernameField.setText(""); // Limpiar el campo de búsqueda después de la operación
    }

    // NUEVO: Método para manejar la visualización de información de un usuario sugerido
    private void guiHandleViewSuggInfo(){
        if (currentUserLoggedIn == null || friendSuggestionsTable == null) return;
        int selectedRow = friendSuggestionsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un usuario de la lista de sugerencias.", "Acción Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String suggestedUsername = (String) friendSuggestionsTableModel.getValueAt(selectedRow, 0);
        User suggestedUser = biblioteca.findUserByUsername(suggestedUsername);

        if (suggestedUser == null) {
            JOptionPane.showMessageDialog(this, "No se pudo encontrar la información del usuario: " + suggestedUsername, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        DoubleList<String> preferredCategories = biblioteca.getPreferredCategories(suggestedUser);
        StringBuilder info = new StringBuilder();
        info.append("Información del Usuario:\n");
        info.append("Username: ").append(suggestedUser.getUsername()).append("\n");
        info.append("Nombre: ").append(suggestedUser.getNombre()).append(" ").append(suggestedUser.getApellido()).append("\n");
        info.append("Categorías Favoritas: ");
        if (preferredCategories.isEmpty()) {
            info.append("No especificadas aún.\n\n");
        } else {
            for (int i = 0; i < preferredCategories.size(); i++) {
                info.append(preferredCategories.get(i));
                if (i < preferredCategories.size() - 1) {
                    info.append(", ");
                }
            }
            info.append("\n\n");
        }
        info.append("¿Desea enviar una solicitud de amistad a este usuario?");

        int option = JOptionPane.showConfirmDialog(this, 
                                                 info.toString(), 
                                                 "Confirmar Envío de Solicitud", 
                                                 JOptionPane.YES_NO_OPTION, 
                                                 JOptionPane.QUESTION_MESSAGE);

        if (option == JOptionPane.YES_OPTION) {
            String result = biblioteca.sendFriendRequest(currentUserLoggedIn.getUsername(), suggestedUser.getUsername());
            JOptionPane.showMessageDialog(this, result, "Enviar Solicitud de Amistad", JOptionPane.INFORMATION_MESSAGE);
            logMessage("Social: Intento de enviar solicitud a '" + suggestedUser.getUsername() + "' desde sugerencias. Resultado: " + result);
            refreshAllSocialTables(); // Actualizar todas las tablas sociales
        } else {
            logMessage("Social: Envío de solicitud a '" + suggestedUser.getUsername() + "' desde sugerencias cancelado.");
        }
    }

    // MÉTODOS RESTAURADOS
    private void guiHandleAcceptFriendRequest() {
        if (currentUserLoggedIn == null) return;
        int selectedRow = pendingRequestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una solicitud de la lista de recibidas.", "Acción Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String requesterUsername = (String) pendingRequestsTableModel.getValueAt(selectedRow, 0); // Columna 0 es el remitente

        String result = biblioteca.acceptFriendRequest(currentUserLoggedIn.getUsername(), requesterUsername);
        JOptionPane.showMessageDialog(this, result, "Aceptar Solicitud de Amistad", JOptionPane.INFORMATION_MESSAGE);
        logMessage("Social: Intento de aceptar solicitud de '" + requesterUsername + "'. Resultado: " + result);
        refreshAllSocialTables();
    }

    private void guiHandleRejectFriendRequest() {
        if (currentUserLoggedIn == null) return;
        int selectedRow = pendingRequestsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione una solicitud de la lista de recibidas.", "Acción Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String requesterUsername = (String) pendingRequestsTableModel.getValueAt(selectedRow, 0);

        String result = biblioteca.rejectFriendRequest(currentUserLoggedIn.getUsername(), requesterUsername);
        JOptionPane.showMessageDialog(this, result, "Rechazar Solicitud de Amistad", JOptionPane.INFORMATION_MESSAGE);
        logMessage("Social: Intento de rechazar solicitud de '" + requesterUsername + "'. Resultado: " + result);
        refreshAllSocialTables();
    }

    private void guiHandleRemoveFriend() {
        if (currentUserLoggedIn == null) return;
        int selectedRow = friendsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Por favor, seleccione un amigo de su lista.", "Acción Requerida", JOptionPane.WARNING_MESSAGE);
            return;
        }
        String friendUsername = (String) friendsTableModel.getValueAt(selectedRow, 0); // Columna 0 es el amigo

        int confirmation = JOptionPane.showConfirmDialog(this, 
            "¿Está seguro de que desea eliminar a '" + friendUsername + "' de su lista de amigos?", 
            "Confirmar Eliminación de Amigo", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);

        if (confirmation == JOptionPane.YES_OPTION) {
            String result = biblioteca.removeFriend(currentUserLoggedIn.getUsername(), friendUsername);
            JOptionPane.showMessageDialog(this, result, "Eliminar Amigo", JOptionPane.INFORMATION_MESSAGE);
            logMessage("Social: Intento de eliminar amigo '" + friendUsername + "'. Resultado: " + result);
            refreshAllSocialTables();
        }
    }
    // FIN DE MÉTODOS RESTAURADOS
}
