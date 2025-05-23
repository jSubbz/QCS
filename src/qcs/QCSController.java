package qcs;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.text.Font;

/**
 * Enhanced QCSController for Quantum Circuit Simulator.
 * Implements selectable gates, language toggle, theme switch,
 * grid resizing, and a columnar gate layout.
 */
public class QCSController extends Application {
    private int qubits = 3;
    private int steps = 5;
    private GridPane circuitGrid;
    private Label modeLabel;
    private boolean designMode = true;
    private String selectedGate = "H";
    private boolean isEnglish = true;
    private boolean isDarkTheme = false;
    private BorderPane root;
    private Scene scene;

    private Button btnMode, btnLang, btnTheme, btnAbout, resizeBtn;
    private Label gateLabel;
    private TextField statusField;

    /**
     * Entry point for the JavaFX application.
     */
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("QCS Simulator - StrangePerry");

        root = new BorderPane();

        // Top bar with mode toggle, language, theme, about
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(10));
        btnMode = new Button();
        btnLang = new Button();
        btnTheme = new Button();
        btnAbout = new Button();
        modeLabel = new Label();

        btnMode.setOnAction(e -> toggleMode());
        btnLang.setOnAction(e -> toggleLanguage());
        btnTheme.setOnAction(e -> toggleTheme());
        btnAbout.setOnAction(e -> showAbout());

        topBar.getChildren().addAll(btnMode, btnLang, btnTheme, btnAbout, modeLabel);

        // Gate panel with a single column layout
        VBox gatePanel = new VBox(10);
        gatePanel.setPadding(new Insets(10));
        gatePanel.setAlignment(Pos.TOP_CENTER);
        gateLabel = new Label();
        gatePanel.getChildren().add(gateLabel);

        String[] gates = {"I", "X", "Y", "Z", "H", "S", "T", "U", "CX", "SWAP", "CU", "CCX", "BARRIER"};
        for (String gate : gates) {
            Button btn = new Button(gate);
            btn.setOnAction(e -> selectedGate = gate);
            btn.setMaxWidth(Double.MAX_VALUE);
            gatePanel.getChildren().add(btn);
        }

        // Grid for circuit
        circuitGrid = new GridPane();
        circuitGrid.setGridLinesVisible(true);
        circuitGrid.setPadding(new Insets(10));
        drawGrid(qubits, steps);

        // Grid resizing controls
        HBox resizeControls = new HBox(10);
        Spinner<Integer> spinQubits = new Spinner<>(1, 10, qubits);
        Spinner<Integer> spinSteps = new Spinner<>(1, 20, steps);
        resizeBtn = new Button();
        resizeBtn.setOnAction(e -> {
            qubits = spinQubits.getValue();
            steps = spinSteps.getValue();
            drawGrid(qubits, steps);
            updateStatus(isEnglish ? "Grid resized." : "Gittergröße geändert.");
        });
        resizeControls.getChildren().addAll(new Label("Qubits:"), spinQubits,
                new Label("Steps:"), spinSteps, resizeBtn);
        resizeControls.setAlignment(Pos.CENTER);

        VBox centerBox = new VBox(10, circuitGrid, resizeControls);
        centerBox.setAlignment(Pos.CENTER);

        root.setTop(topBar);
        root.setLeft(gatePanel);
        root.setCenter(centerBox);

        // Status bar at bottom
        statusField = new TextField();
        statusField.setEditable(false);
        root.setBottom(statusField);

        scene = new Scene(root, 900, 600);
        applyTheme();
        updateLanguage();

        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();
    }

    /**
     * Draws the grid with buttons that allow gate placement.
     */
    private void drawGrid(int qubits, int steps) {
        circuitGrid.getChildren().clear();
        for (int row = 0; row < qubits; row++) {
            for (int col = 0; col < steps; col++) {
                Button cell = new Button("");
                cell.setPrefSize(80, 40);
                int finalRow = row;
                int finalCol = col;
                cell.setOnAction(e -> {
                    if (designMode) {
                        cell.setText(selectedGate);
                        updateStatus((isEnglish ? "Gate " : "Gatter ") + selectedGate +
                                (isEnglish ? " placed at " : " platziert bei ") + finalRow + "," + finalCol);
                    }
                });
                circuitGrid.add(cell, col, row);
            }
        }
    }

    /**
     * Toggles between design and play mode.
     */
    private void toggleMode() {
        designMode = !designMode;
        updateLanguage();
    }

    /**
     * Switches between English and German.
     */
    private void toggleLanguage() {
        isEnglish = !isEnglish;
        updateLanguage();
    }

    /**
     * Updates all language-dependent UI text.
     */
    private void updateLanguage() {
        btnMode.setText((isEnglish ? "Switch to " : "Wechsel zu ") + (designMode ? "Play" : "Design") + (isEnglish ? " Mode" : " Modus"));
        btnLang.setText(isEnglish ? "DE" : "EN");
        btnTheme.setText(isEnglish ? (isDarkTheme ? "Light" : "Dark") : (isDarkTheme ? "Hell" : "Dunkel"));
        btnAbout.setText(isEnglish ? "About" : "Über");
        gateLabel.setText(isEnglish ? "Quantum Gates" : "Quanten-Gatter");
        resizeBtn.setText(isEnglish ? "Resize" : "Größe ändern");
        modeLabel.setText((isEnglish ? "Current Mode: " : "Modus: ") + (designMode ? "Design" : "Play"));
        updateStatus(isEnglish ? "Language changed to English." : "Sprache auf Deutsch geändert.");
    }

    /**
     * Applies dark or light theme to the scene.
     */
    private void toggleTheme() {
        isDarkTheme = !isDarkTheme;
        applyTheme();
        updateLanguage();
    }

    private void applyTheme() {
        scene.getStylesheets().clear();
        if (isDarkTheme) {
            var dark = getClass().getResource("dark-theme.css");
            if (dark != null) scene.getStylesheets().add(dark.toExternalForm());
        } else {
            var light = getClass().getResource("light-theme.css");
            if (light != null) scene.getStylesheets().add(light.toExternalForm());
        }
    }

    /**
     * Shows an About dialog.
     */
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(isEnglish ? "About" : "Über");
        alert.setHeaderText(isEnglish ? "Quantum Circuit Simulator" : "Quanten-Schaltkreis-Simulator");
        alert.setContentText(isEnglish
                ? "Created by Team StrangePerry for CST8221 Summer 2025."
                : "Erstellt von Team StrangePerry für CST8221 Sommer 2025.");
        alert.showAndWait();
    }

    /**
     * Updates the bottom status text.
     */
    private void updateStatus(String message) {
        statusField.setText(message);
    }

    /**
     * Launches the JavaFX application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
