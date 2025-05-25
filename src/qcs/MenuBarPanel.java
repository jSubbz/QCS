package qcs;

import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.io.File;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * MenuBarPanel handles the menu bar UI and its actions.
 * It supports New Project, Open, Save, Exit, Settings, and Help menus.
 */
public class MenuBarPanel implements EventBus.EventListener {
    private MenuBar menuBar;
    private MenuItem toggleModeItem;
    private QuantumCircuitPanel gridPanel;

    /**
     * Constructor that accepts a reference to the QuantumCircuitPanel for grid actions.
     * @param gridPanel the main panel to manage patterns
     */
    public MenuBarPanel(QuantumCircuitPanel gridPanel) {
        this.gridPanel = gridPanel;
        rebuildMenuBar();  // Build the menu bar with current language
        EventBus.getInstance().subscribe(this);  // Subscribe to events
    }

    public void rebuildMenuBar() {
        // 🔥 FIX: Load fresh ResourceBundle each time rebuildMenuBar is called
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
        exitItem.setOnAction(e -> System.exit(0));

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
        helpMenu.getItems().addAll(
                new MenuItem(bundle.getString("about")),
                new MenuItem(bundle.getString("readMe"))
        );

        menuBar.getMenus().addAll(fileMenu, settingsMenu, helpMenu);
    }

    public MenuBar getMenuBar() {
        return menuBar;
    }

    @Override
    public void onEvent(String eventType) {
        if ("languageChanged".equals(eventType) || "modeToggled".equals(eventType)) {
            rebuildMenuBar();  // 🔥 Rebuild with updated language
        }
    }

    /** Handles New Project: clears the grid with confirmation. */
    private void handleNewProject() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();  // 🔥 Use fresh bundle
        if (gridPanel.hasActiveCells()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    bundle.getString("confirmNewProject"),
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle(bundle.getString("menuNew"));
            var result = confirm.showAndWait();
            if (result.isEmpty() || result.get() != ButtonType.YES) {
                return; // User canceled
            }
        }
        gridPanel.clearGrid();
    }

    /** Handles Save: prompts for a file and saves the grid. */
    private void handleSave() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();  // 🔥 Use fresh bundle
        FileChooser chooser = new FileChooser();
        chooser.setTitle(bundle.getString("savePattern"));
        File file = chooser.showSaveDialog(null);
        if (file != null) {
            gridPanel.savePatternToFile(file);
        }
    }

    /** Handles Open: prompts for a file and loads the grid. */
    private void handleOpen() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();  // 🔥 Use fresh bundle
        FileChooser chooser = new FileChooser();
        chooser.setTitle(bundle.getString("loadPattern"));
        File file = chooser.showOpenDialog(null);
        if (file != null) {
            gridPanel.loadPatternFromFile(file);
        }
    }
}
