package qcs.controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Socket;

import qcs.network.CircuitDataRequest;
import qcs.network.ServerResponse;

import javafx.scene.layout.VBox;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import qcs.util.EventBus;
import qcs.util.PropertiesValidator;
import qcs.util.SettingsManager;
import qcs.view.*;

import java.io.File;
import java.io.IOException;

/**
 * MainDriver serves as the entry point of the QCS application.
 * It sets up the JavaFX stage and scene, initializes various panels,
 * subscribes to relevant events, and manages application settings such as themes and language.
 */
public class MainDriver extends Application implements EventBus.EventListener {
    private BorderPane root;
    private Scene scene;
    private MenuBarPanel menuBarPanel;
    private QuantumCircuitPanel quantumCircuitPanel;  // Moved to field if needed elsewhere

    /**
     * Starts the JavaFX application, setting up the UI layout, panels, and event subscriptions.
     *
     * @param primaryStage the primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        EventBus.getInstance().subscribe(this);

        root = new BorderPane();

        quantumCircuitPanel = new QuantumCircuitPanel();  // Instantiate this first
        menuBarPanel = new MenuBarPanel(quantumCircuitPanel);  // Now pass it here
        root.setTop(menuBarPanel.getMenuBar());

        root.setCenter(quantumCircuitPanel.getPanel());

        LeftPanel leftPanel = new LeftPanel();
        root.setLeft(leftPanel.getPanel());

        BottomPanel bottomPanel = new BottomPanel();
        VBox bottomAreaMainPanel = bottomPanel.getPanel();
        quantumCircuitPanel.setBottomPanel(bottomPanel);


        root.setBottom(bottomPanel.getPanel());

        scene = new Scene(root, 950, 825);
        bottomAreaMainPanel.prefHeightProperty().bind(scene.heightProperty().multiply(.2)); //Defining max height of the message box
        applyTheme();

        primaryStage.setTitle("Quantum Circuit Simulator - Perry & Strange");
        primaryStage.setScene(scene);

        Splash splash = new Splash();
        splash.showAndSwitch(primaryStage);
    }

    /**
     * Applies the selected theme to the scene by updating its stylesheet.
     * The theme is determined by the current settings in SettingsManager.
     */
    private void applyTheme() {
        scene.getStylesheets().clear();
        String themeFile = String.format("/qcs/themes/%s.css", SettingsManager.getInstance().getTheme());
        var resource = getClass().getResource(themeFile);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    /**
     * Handles events published to the EventBus.
     * Responds to language changes, mode toggles, and theme updates by updating relevant UI elements.
     *
     * @param eventType the type of event that occurred.
     */
    @Override
    public void onEvent(String eventType) {
        if ("languageChanged".equals(eventType) || "modeToggled".equals(eventType)) {
            menuBarPanel.rebuildMenuBar();
            root.setTop(menuBarPanel.getMenuBar());  // Replace old menu with new one
        } else if ("themeChanged".equals(eventType)) {
            applyTheme();  // Update the Scene's stylesheet
        }
    }


    public static void sendTestCircuit(String username) {
        try (Socket socket = new Socket("localhost", 12345);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            String fakeCircuitJson = "{\"qubits\":2,\"gates\":[{\"type\":\"H\",\"target\":0}]}";

            CircuitDataRequest request = new CircuitDataRequest(username, fakeCircuitJson);
            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            if (response instanceof ServerResponse res) {
                System.out.println("Server reply: " + res.getMessage());
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to send circuit: " + e.getMessage());
        }
    }

    /**
     * The main method of the application.
     * It checks for duplicate property keys in resource files and launches the JavaFX application.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        File dir = new File("src/qcs");
        File[] propertyFiles = dir.listFiles((d, name) -> name.endsWith(".properties"));
        if (propertyFiles != null) {
            for (File file : propertyFiles) {
                try {
                    PropertiesValidator.checkForDuplicateKeys(file.getPath());
                } catch (IOException e) {
                    System.err.println("Error reading " + file.getPath() + ": " + e.getMessage());
                }
            }
        }
        launch(args);
    }
}
