package qcs;
import qcs.EventBus;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

public class MainDriver extends Application implements EventBus.EventListener {
    private BorderPane root;
    private Scene scene;
    private MenuBarPanel menuBarPanel;

    @Override
    public void start(Stage primaryStage) {
        EventBus.getInstance().subscribe(this);

        root = new BorderPane();

        menuBarPanel = new MenuBarPanel();
        root.setTop(menuBarPanel.getMenuBar());

        QuantumCircuitPanel quantumCircuitPanel = new QuantumCircuitPanel();
        root.setCenter(quantumCircuitPanel.getPanel());

        LeftPanel leftPanel = new LeftPanel();
        root.setLeft(leftPanel.getPanel());

        BottomPanel bottomPanel = new BottomPanel();
        root.setBottom(bottomPanel.getPanel());

        scene = new Scene(root, 900, 600);
        applyTheme();

        primaryStage.setTitle("QCS Refactored");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void applyTheme() {
        scene.getStylesheets().clear();
        String themeFile = String.format("themes/%s.css", SettingsManager.getInstance().getTheme());
        var resource = getClass().getResource(themeFile);
        if (resource != null) {
            scene.getStylesheets().add(resource.toExternalForm());
        }
    }

    @Override
    public void onEvent(String eventType) {
        if ("languageChanged".equals(eventType) || "modeToggled".equals(eventType)) {
            menuBarPanel.rebuildMenuBar();
            root.setTop(menuBarPanel.getMenuBar());  // Replace old menu with new one
        } else if ("themeChanged".equals(eventType)) {
            applyTheme();  // Update the Scene's stylesheet
        }
    }

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