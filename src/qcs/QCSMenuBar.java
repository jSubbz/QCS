package qcs;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.MessageFormat;
import java.util.*;

public class QCSMenuBar extends Application {

    private String currentLanguage = "en"; // Default language
    private ResourceBundle bundle;
    private String currentTheme = "dark-mode"; // Default theme
    private static final List<String> AVAILABLE_THEMES = List.of(
            "light-mode", "dark-mode", "protanopia", "deuteranopia", "tritanopia", "achromatopsia"
    );

    private BorderPane root;
    private Scene scene;
    private Label gateLabel;
    private Label modeLabel;
    private Button resizeBtn;
    private GridPane circuitGrid;
    private MenuBar menuBar; // Store menuBar to refresh dynamically
    private boolean designMode = true;
    private int qubits = 3;
    private int steps = 5;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("QCS Menu Bar Enhanced");

        root = new BorderPane();
        menuBar = createMenuBar();
        root.setTop(menuBar);

        circuitGrid = new GridPane();
        circuitGrid.setGridLinesVisible(true);
        circuitGrid.setPadding(new Insets(10));
        drawGrid(qubits, steps);

        HBox resizeControls = createResizeControls();
        VBox centerBox = new VBox(10, circuitGrid, resizeControls);
        centerBox.setAlignment(Pos.CENTER);
        root.setCenter(centerBox);

        gateLabel = new Label();
        modeLabel = new Label(); // Will be set in updateLanguage()
        VBox leftPanel = new VBox(10, gateLabel, modeLabel);
        leftPanel.setPadding(new Insets(10));
        root.setLeft(leftPanel);

        TextField statusField = new TextField();
        statusField.setEditable(false);
        root.setBottom(statusField);

        scene = new Scene(root, 900, 600);
        applyTheme();
        updateLanguage();

        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private MenuBar createMenuBar() {
        MenuBar bar = new MenuBar();

        Menu fileMenu = new Menu(bundle != null ? bundle.getString("menuFile") : "File");
        MenuItem newItem = new MenuItem(bundle != null ? bundle.getString("menuNew") : "New Project");
        MenuItem openItem = new MenuItem(bundle != null ? bundle.getString("menuOpen") : "Open...");
        MenuItem saveItem = new MenuItem(bundle != null ? bundle.getString("menuSave") : "Save");
        MenuItem exitItem = new MenuItem(bundle != null ? bundle.getString("menuExit") : "Exit");
        exitItem.setOnAction(e -> System.exit(0));
        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);

        Menu settingsMenu = new Menu(bundle != null ? bundle.getString("menuSettings") : "Settings");
        MenuItem settingsWindowItem = new MenuItem(bundle != null ? bundle.getString("menuSettingsWindow") : "Open Settings Window");
        settingsWindowItem.setOnAction(e -> openSettingsWindow());

        // Initialize toggleModeItem with the correct initial mode text
        String initialMode = designMode ? "Design" : "Play";
        MenuItem toggleModeItem = new MenuItem(MessageFormat.format(bundle != null ? bundle.getString("menuToggleMode") : "{0} Mode", initialMode));
        toggleModeItem.setOnAction(e -> toggleMode(toggleModeItem));

        settingsMenu.getItems().addAll(settingsWindowItem, toggleModeItem);


        Menu helpMenu = new Menu(bundle != null ? bundle.getString("menuHelp") : "Help");
        MenuItem aboutItem = new MenuItem(bundle != null ? bundle.getString("about") : "About");
        aboutItem.setOnAction(e -> showAbout());
        MenuItem readmeItem = new MenuItem(bundle != null ? bundle.getString("readMe") : "ReadMe");
        readmeItem.setOnAction(e -> showReadMe());
        helpMenu.getItems().addAll(aboutItem, readmeItem);

        bar.getMenus().addAll(fileMenu, settingsMenu, helpMenu);
        return bar;
    }

    private HBox createResizeControls() {
        Spinner<Integer> spinQubits = new Spinner<>(1, 10, qubits);
        Spinner<Integer> spinSteps = new Spinner<>(1, 20, steps);
        resizeBtn = new Button();
        resizeBtn.setOnAction(e -> {
            qubits = spinQubits.getValue();
            steps = spinSteps.getValue();
            drawGrid(qubits, steps);
            updateStatus(bundle.getString("resize"));
        });
        HBox resizeControls = new HBox(10, new Label("Qubits:"), spinQubits, new Label("Steps:"), spinSteps, resizeBtn);
        resizeControls.setAlignment(Pos.CENTER);
        return resizeControls;
    }

    private void updateLanguage() {
        bundle = ResourceBundle.getBundle("qcs.messages", new Locale(currentLanguage));
        String mode = designMode ? "Design" : "Play";
        gateLabel.setText(bundle.getString("quantumGates"));
        resizeBtn.setText(bundle.getString("resize"));
        modeLabel.setText(MessageFormat.format(bundle.getString("currentMode"), mode));
        updateStatus(bundle.getString("languageChanged"));

        // Refresh the menu bar
        root.setTop(null);
        menuBar = createMenuBar();
        root.setTop(menuBar);
    }

    private void applyTheme() {
        scene.getStylesheets().clear();
        String themeFile = String.format("themes/%s.css", currentTheme);
        var resource = getClass().getResource(themeFile);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        } else {
            System.err.println("Theme not found: " + themeFile);
        }
    }

    private void toggleMode(MenuItem item) {
        designMode = !designMode;
        String newMode = designMode ? "Design" : "Play";
        item.setText(MessageFormat.format(bundle.getString("menuToggleMode"), newMode));
        modeLabel.setText(MessageFormat.format(bundle.getString("currentMode"), newMode));
        updateStatus("Mode switched to " + newMode);
    }

    private void openSettingsWindow() {
        Stage settingsStage = new Stage();
        settingsStage.setTitle(bundle.getString("settingsWindowTitle"));

        VBox settingsLayout = new VBox(10);
        settingsLayout.setPadding(new Insets(10));

        ComboBox<String> languageComboBox = new ComboBox<>();
        languageComboBox.getItems().addAll("en", "de", "fr");
        languageComboBox.setValue(currentLanguage);
        languageComboBox.setOnAction(e -> {
            currentLanguage = languageComboBox.getValue();
            updateLanguage();
        });

        ComboBox<String> themeComboBox = new ComboBox<>();
        themeComboBox.getItems().addAll(AVAILABLE_THEMES);
        themeComboBox.setValue(currentTheme);
        themeComboBox.setOnAction(e -> {
            currentTheme = themeComboBox.getValue();
            applyTheme();
        });

        settingsLayout.getChildren().addAll(
                new Label(bundle.getString("language")), languageComboBox,
                new Label(bundle.getString("settingsTheme")), themeComboBox
        );

        Scene settingsScene = new Scene(settingsLayout, 300, 200);
        settingsStage.setScene(settingsScene);
        settingsStage.initOwner(root.getScene().getWindow());
        settingsStage.initModality(Modality.APPLICATION_MODAL);
        settingsStage.showAndWait();
    }

    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("about"));
        alert.setHeaderText(bundle.getString("aboutHeader"));
        alert.setContentText(bundle.getString("aboutContent"));
        alert.showAndWait();
    }

    private void showReadMe() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("readMe"));
        alert.setHeaderText(bundle.getString("readMeHeader"));
        alert.setContentText(bundle.getString("readMeContent"));
        alert.showAndWait();
    }

    private void updateStatus(String message) {
        if (root.getBottom() instanceof TextField) {
            ((TextField) root.getBottom()).setText(message);
        }
    }

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
                        cell.setText("H");
                        updateStatus((currentLanguage.equals("de") ? "Gatter " : "Gate ") + "H placed at " + finalRow + "," + finalCol);
                    }
                });
                circuitGrid.add(cell, col, row);
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
