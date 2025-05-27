package org.example;

import org.example.model.Book;
import org.example.model.Loan;
import org.example.model.LoanRequest;
import org.example.model.User;
import org.example.model.enums.TipoUsuario;
import org.example.structures.doubleList.DoubleList;
import org.example.structures.colaPrioridad.ColaPrioridad;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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

        // --- Pestaña 2: Gestión de Usuarios (Visible solo para Administradores) ---
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            tabbedPane.addTab("Gestión de Usuarios", createUsersPanel());
        }
        
        // --- Pestaña 3: Operaciones (Visible para Lectores y Administradores) ---
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR || currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
             tabbedPane.addTab("Operaciones", createOperationsPanel());
        }

        // --- Pestaña 4: Red Social (Visible para Lectores) ---
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR) {
            // JPanel socialPanel = createSocialPanel(); // A implementar
            // tabbedPane.addTab("Red Social", socialPanel);
        }
        
        // --- Pestaña 5: Panel de Administrador (Solo para Admin)
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            // JPanel adminPanel = createAdminPanel(); // A implementar
            // tabbedPane.addTab("Panel Administrador", adminPanel);
        }

        // --- Pestaña de Recomendaciones (Visible para Lectores) ---
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR) {
            tabbedPane.addTab("Mis Recomendaciones", createRecommendationsPanel());
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
        
        // Cargar datos iniciales en las áreas de texto si es apropiado
        if (tabbedPane.getTabCount() > 0 && bookCatalogTable != null) { // Asegurarse que la pestaña de libros existe
             loadBooks();
        }
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR && usersTextArea != null) { // Y la de usuarios si es admin
            loadUsers();
        }
        // Cargar datos para la pestaña de operaciones si está visible
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR || currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
            if(opUsernameField != null && currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR) {
                opUsernameField.setText(currentUserLoggedIn.getUsername());
                opUsernameField.setEditable(false); // Lector no puede cambiar su username aquí
            } else if (opUsernameField != null && currentUserLoggedIn.getTipoUsuario() == TipoUsuario.ADMINISTRADOR) {
                opUsernameField.setEditable(true); // Admin puede especificar para quién es la operación
            }
            loadUserLoansGUI(); // Cargar préstamos del usuario actual si es LECTOR
            loadGlobalWaitlistGUI();
        }
        // Cargar recomendaciones si la pestaña está visible y el usuario es LECTOR
        if (currentUserLoggedIn.getTipoUsuario() == TipoUsuario.LECTOR && recommendationsTable != null) {
            loadRecommendationsGUI();
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
            JPanel addBookPanel = new JPanel(new GridBagLayout());
            addBookPanel.setBorder(BorderFactory.createTitledBorder("Añadir Nuevo Libro"));
            GridBagConstraints gbcAddBook = new GridBagConstraints();
            gbcAddBook.insets = new Insets(3,3,3,3);
            gbcAddBook.fill = GridBagConstraints.HORIZONTAL;

            // Labels y Fields para añadir libro
            gbcAddBook.gridx = 0; gbcAddBook.gridy = 0; addBookPanel.add(new JLabel("ID:"), gbcAddBook);
            bookIdField = new JTextField(15); gbcAddBook.gridx = 1; gbcAddBook.gridy = 0; addBookPanel.add(bookIdField, gbcAddBook);
            
            gbcAddBook.gridx = 0; gbcAddBook.gridy = 1; addBookPanel.add(new JLabel("Título:"), gbcAddBook);
            bookTitleField = new JTextField(15); gbcAddBook.gridx = 1; gbcAddBook.gridy = 1; addBookPanel.add(bookTitleField, gbcAddBook);

            gbcAddBook.gridx = 0; gbcAddBook.gridy = 2; addBookPanel.add(new JLabel("Autor:"), gbcAddBook);
            bookAuthorField = new JTextField(15); gbcAddBook.gridx = 1; gbcAddBook.gridy = 2; addBookPanel.add(bookAuthorField, gbcAddBook);

            gbcAddBook.gridx = 0; gbcAddBook.gridy = 3; addBookPanel.add(new JLabel("Categoría:"), gbcAddBook);
            bookCategoryField = new JTextField(15); gbcAddBook.gridx = 1; gbcAddBook.gridy = 3; addBookPanel.add(bookCategoryField, gbcAddBook);

            gbcAddBook.gridx = 0; gbcAddBook.gridy = 4; addBookPanel.add(new JLabel("Año Pub.:"), gbcAddBook);
            bookYearField = new JTextField(15); gbcAddBook.gridx = 1; gbcAddBook.gridy = 4; addBookPanel.add(bookYearField, gbcAddBook);
            
            gbcAddBook.gridx = 0; gbcAddBook.gridy = 5; addBookPanel.add(new JLabel("Stock Inicial:"), gbcAddBook); // Nuevo campo para stock
            bookStockField = new JTextField(15); gbcAddBook.gridx = 1; gbcAddBook.gridy = 5; addBookPanel.add(bookStockField, gbcAddBook);

            addBookButton = new JButton("Añadir Libro");
            gbcAddBook.gridx = 0; gbcAddBook.gridy = 6; gbcAddBook.gridwidth = 2; gbcAddBook.fill = GridBagConstraints.NONE; gbcAddBook.anchor = GridBagConstraints.CENTER;
            addBookPanel.add(addBookButton, gbcAddBook);
            addBookButton.addActionListener(e -> addBook());
            
            leftBookPanel.add(addBookPanel, BorderLayout.SOUTH);
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
                if (book != null) {
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
        // Validar que los campos no estén vacíos (simplificado)
        String id = bookIdField.getText();
        String title = bookTitleField.getText();
        String author = bookAuthorField.getText();
        String category = bookCategoryField.getText();
        String yearStr = bookYearField.getText();
        String stockStr = bookStockField.getText(); // Obtener stock

        if (id.isEmpty() || title.isEmpty() || author.isEmpty() || category.isEmpty() || yearStr.isEmpty() || stockStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error al Añadir Libro", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int year = Integer.parseInt(yearStr);
            int stock = Integer.parseInt(stockStr); // Convertir stock a int

            if (stock < 0) {
                 JOptionPane.showMessageDialog(this, "El stock no puede ser negativo.", "Error al Añadir Libro", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Book newBook = new Book(id, title, author, category, year, stock); // Usar constructor con stock
            biblioteca.addBook(newBook);
            logMessage("Libro añadido: " + title + " (ID: " + id + ", Stock: " + stock + ")");
            loadBooks(); // Recargar la tabla para mostrar el nuevo libro

            // Limpiar campos después de añadir
            bookIdField.setText("");
            bookTitleField.setText("");
            bookAuthorField.setText("");
            bookCategoryField.setText("");
            bookYearField.setText("");
            bookStockField.setText("");

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El año y el stock deben ser números válidos.", "Error de Formato", JOptionPane.ERROR_MESSAGE);
        }
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
}
