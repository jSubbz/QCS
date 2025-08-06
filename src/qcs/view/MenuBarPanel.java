package qcs.view;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import qcs.network.CircuitDataRequest;
import qcs.network.NetworkClient;
import qcs.network.ServerResponse;
import qcs.util.SettingsManager;
import qcs.util.EventBus;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * MenuBarPanel handles the menu bar UI and its actions.
 * It supports New Project, Open, Save, Exit, Settings, and Help menus,
 * and updates dynamically with language and mode changes.
 */
public class MenuBarPanel implements EventBus.EventListener {
    private MenuBar menuBar;
    private MenuItem toggleModeItem;
    private QuantumCircuitPanel gridPanel;

    /**
     * Constructs the MenuBarPanel, initializing its menus and subscribing to EventBus events.
     *
     * @param gridPanel the QuantumCircuitPanel associated with this menu, used for grid actions.
     */
    public MenuBarPanel(QuantumCircuitPanel gridPanel) {
        this.gridPanel = gridPanel;
        rebuildMenuBar();  // Build the menu bar with current language
        EventBus.getInstance().subscribe(this);  // Subscribe to events
    }

    /**
     * Rebuilds the menu bar using the latest ResourceBundle for localization.
     * Called initially and when language or mode changes.
     */
    public void rebuildMenuBar() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();

        menuBar = new MenuBar();

        // File Menu
        Menu fileMenu = new Menu(bundle.getString("menuFile"));

        MenuItem newItem = new MenuItem(bundle.getString("menuNew"));
        newItem.setOnAction(e -> handleNewProject());

        MenuItem openItem = new MenuItem(bundle.getString("menuOpen"));
        openItem.setOnAction(e -> handleOpen());

        MenuItem saveItem = new MenuItem(bundle.getString("menuSave"));
        saveItem.setOnAction(e -> handleSave());

        MenuItem exitItem = new MenuItem(bundle.getString("menuExit"));
        exitItem.setOnAction(e -> {
            // Cleanly disconnect when exiting
            NetworkClient.getInstance().disconnect();
            System.exit(0);
        });

        fileMenu.getItems().addAll(newItem, openItem, saveItem, new SeparatorMenuItem(), exitItem);

        // Settings Menu
        Menu settingsMenu = new Menu(bundle.getString("menuSettings"));
        MenuItem settingsWindowItem = new MenuItem(bundle.getString("menuSettingsWindow"));
        settingsWindowItem.setOnAction(e -> new SettingsWindow().show());

        String targetMode = SettingsManager.getInstance().isDesignMode()
                ? bundle.getString("playMode") : bundle.getString("designMode");
        toggleModeItem = new MenuItem(MessageFormat.format(bundle.getString("menuToggleMode"), targetMode));
        toggleModeItem.setOnAction(e -> {
            SettingsManager.getInstance().toggleMode();
            EventBus.getInstance().publish("modeToggled");
            System.out.println("Toggled to " + (SettingsManager.getInstance().isDesignMode() ? "Design" : "Play") + " mode.");
        });
        settingsMenu.getItems().addAll(settingsWindowItem, toggleModeItem);

        // Help Menu
        Menu helpMenu = new Menu(bundle.getString("menuHelp"));
        MenuItem aboutItem = new MenuItem(bundle.getString("about"));
        aboutItem.setOnAction(e -> handleAbout());
        MenuItem readMeItem = new MenuItem(bundle.getString("readMe"));  // Placeholder
        helpMenu.getItems().addAll(aboutItem, readMeItem);

        menuBar.getMenus().addAll(fileMenu, settingsMenu, helpMenu);
    }

    /**
     * Displays the contents of the README.md file in a dialog.
     */
    private void handleAbout() {
        try {
            String readmeContent = Files.readString(Paths.get("README.md"));
            TextArea textArea = new TextArea(readmeContent);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setPrefWidth(600);
            textArea.setPrefHeight(400);

            Alert aboutDialog = new Alert(Alert.AlertType.INFORMATION);
            aboutDialog.setTitle("About Quantum Circuit Simulator - Perry & Strange");
            aboutDialog.setHeaderText("About QCS");
            aboutDialog.getDialogPane().setContent(textArea);
            aboutDialog.showAndWait();
        } catch (IOException e) {
            Alert errorDialog = new Alert(Alert.AlertType.ERROR, "Could not load README.md: " + e.getMessage());
            errorDialog.showAndWait();
        }
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    @Override
    public void onEvent(String eventType) {
        if ("languageChanged".equals(eventType) || "modeToggled".equals(eventType)) {
            rebuildMenuBar();
        }
    }

    private void handleNewProject() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();
        if (gridPanel.hasActiveCells()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    bundle.getString("confirmNewProject"),
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle(bundle.getString("menuNew"));
            var result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.YES) {
                return;
            }
        }
        gridPanel.clearGrid();
    }

    private void handleSave() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();

        Alert saveChoice = new Alert(Alert.AlertType.CONFIRMATION);
        saveChoice.setTitle(bundle.getString("menuSave"));
        saveChoice.setHeaderText("Choose Save Method");
        saveChoice.setContentText("Do you want to save locally or to the database?");

        ButtonType localBtn = new ButtonType("Save Locally");
        ButtonType dbBtn = new ButtonType("Save to Database");
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        saveChoice.getButtonTypes().setAll(localBtn, dbBtn, cancelBtn);

        saveChoice.showAndWait().ifPresent(result -> {
            if (result == localBtn) {
                FileChooser chooser = new FileChooser();
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Quantum Assembly Language", "*.qasm"));
                chooser.setTitle(bundle.getString("savePattern"));
                File file = chooser.showSaveDialog(null);
                if (file != null) {
                    gridPanel.savePatternToFile(file);
                }
            } else if (result == dbBtn) {
                saveToDatabase();
            }
        });
    }

    // --- REFACTORED METHOD ---
    private void saveToDatabase() {
        String username = SettingsManager.getInstance().getUsername();
        if (username == null || username.isEmpty()) {
            showStatus("Error: Cannot save. No user is logged in.");
            return;
        }

        String circuitJson = gridPanel.exportToJson();

        // Create the correct request object for saving circuit data
        CircuitDataRequest request = new CircuitDataRequest(username, circuitJson);

        // Send the request using the persistent network client
        ServerResponse response = NetworkClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            showStatus("✅ Circuit saved to database for user: " + username);
        } else {
            String errorMsg = (response != null) ? response.getMessage() : "No response from server.";
            showStatus("❌ Failed to save circuit to database. " + errorMsg);
        }
    }

    private void showStatus(String message) {
        if (gridPanel != null && gridPanel.getBottomPanel() != null) {
            gridPanel.getBottomPanel().updateStatus(message);
        } else {
            System.out.println(message);
        }
    }

    private void handleOpen() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Quantum Assembly Language", "*.qasm"));
        chooser.setTitle(bundle.getString("loadPattern"));
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            gridPanel.loadPatternFromFile(file);
        }
    }
}