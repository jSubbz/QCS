package qcs;
import qcs.EventBus;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

public class MenuBarPanel implements EventBus.EventListener {
    private MenuBar menuBar;
    private MenuItem toggleModeItem;
    private SettingsWindow settingsWindow;

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
        toggleModeItem = new MenuItem(String.format(bundle.getString("menuToggleMode"), SettingsManager.getInstance().isDesignMode() ? "Design" : "Play"));
        toggleModeItem.setOnAction(e -> {
            SettingsManager.getInstance().toggleMode();
            EventBus.getInstance().publish("modeToggled");
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
