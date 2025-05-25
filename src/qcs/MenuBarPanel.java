package qcs;

import javafx.scene.control.*;
import java.text.MessageFormat;

public class MenuBarPanel implements EventBus.EventListener {
    private MenuBar menuBar;
    private MenuItem toggleModeItem;

    public MenuBarPanel() {
        rebuildMenuBar();
        EventBus.getInstance().subscribe(this);
    }

    public void rebuildMenuBar() {
        var bundle = SettingsManager.getInstance().getBundle();
        menuBar = new MenuBar();

        Menu fileMenu = new Menu(bundle.getString("menuFile"));
        fileMenu.getItems().addAll(
                new MenuItem(bundle.getString("menuNew")),
                new MenuItem(bundle.getString("menuOpen")),
                new MenuItem(bundle.getString("menuSave")),
                new SeparatorMenuItem(),
                new MenuItem(bundle.getString("menuExit"))
        );
        fileMenu.getItems().get(4).setOnAction(e -> System.exit(0));

        Menu settingsMenu = new Menu(bundle.getString("menuSettings"));
        MenuItem settingsWindowItem = new MenuItem(bundle.getString("menuSettingsWindow"));
        settingsWindowItem.setOnAction(e -> new SettingsWindow().show());

        // 🔸 Correct formatting to show target mode (Play if currently Design, and vice versa)
        String targetMode = SettingsManager.getInstance().isDesignMode() ? bundle.getString("playMode") : bundle.getString("designMode");
        toggleModeItem = new MenuItem(MessageFormat.format(bundle.getString("menuToggleMode"), targetMode));
        toggleModeItem.setOnAction(e -> {
            SettingsManager.getInstance().toggleMode();
            EventBus.getInstance().publish("modeToggled");
            System.out.println("Toggled to " + (SettingsManager.getInstance().isDesignMode() ? "Design" : "Play") + " mode.");
        });
        settingsMenu.getItems().addAll(settingsWindowItem, toggleModeItem);

        Menu helpMenu = new Menu(bundle.getString("menuHelp"));
        helpMenu.getItems().addAll(new MenuItem(bundle.getString("about")), new MenuItem(bundle.getString("readMe")));

        menuBar.getMenus().addAll(fileMenu, settingsMenu, helpMenu);
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
}
