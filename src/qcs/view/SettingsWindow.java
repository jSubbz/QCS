package qcs.view;

import qcs.util.EventBus;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import qcs.util.SettingsManager;

import java.util.List;
import java.util.ResourceBundle;

/**
 * SettingsWindow provides a UI window for selecting the language and theme settings of the application.
 * It dynamically updates in response to changes and supports multiple predefined languages and themes.
 */
public class SettingsWindow implements EventBus.EventListener {
    private static final List<String> AVAILABLE_LANGUAGES = List.of("en", "de", "fr");
    private static final List<String> AVAILABLE_THEMES = List.of(
            "dark-mode", "light-mode", "protanopia", "deuteranopia", "tritanopia", "achromatopsia"
    );

    private Stage stage;
    private VBox layout;
    private Scene scene;
    private ToggleGroup languageGroup;
    private ToggleGroup themeGroup;
    private Label languageLabel;
    private Label themeLabel;

    /**
     * Constructs the SettingsWindow and subscribes to EventBus for updates.
     */
    public SettingsWindow() {
        EventBus.getInstance().subscribe(this); // Subscribe to events
    }

    /**
     * Shows the settings window, initializing the UI components for language and theme selection.
     */
    public void show() {
        stage = new Stage();
        layout = new VBox(10);
        layout.setPadding(new Insets(10));

        updateUI(); // Build UI elements based on the current settings

        scene = new Scene(layout, 300, 400);
        scene.getStylesheets().add(getClass().getResource("/qcs/themes/" + SettingsManager.getInstance().getTheme() + ".css").toExternalForm());
        layout.getStyleClass().add("settings-window");

        stage.setTitle(SettingsManager.getInstance().getBundle().getString("settingsWindowTitle"));
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    /**
     * Updates the UI components to reflect the current language and theme.
     * Called initially and when a language or theme change event occurs.
     */
    private void updateUI() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();

        layout.getChildren().clear(); // Clear existing nodes

        // Language Section
        languageLabel = new Label(bundle.getString("selectLanguage"));
        layout.getChildren().add(languageLabel);

        languageGroup = new ToggleGroup();
        String currentLanguage = SettingsManager.getInstance().getLanguage();

        for (String lang : AVAILABLE_LANGUAGES) {
            String localizedLang = switch (lang) {
                case "en" -> bundle.getString("languageEnglish");
                case "de" -> bundle.getString("languageGerman");
                case "fr" -> bundle.getString("languageFrench");
                default -> lang;
            };
            RadioButton langBtn = new RadioButton(localizedLang);
            langBtn.setUserData(lang);
            langBtn.setToggleGroup(languageGroup);
            if (lang.equals(currentLanguage)) langBtn.setSelected(true);
            langBtn.setOnAction(e -> {
                SettingsManager.getInstance().setLanguage((String) langBtn.getUserData());
                EventBus.getInstance().publish("languageChanged");
            });
            layout.getChildren().add(langBtn);
        }

        // Theme Section
        themeLabel = new Label(bundle.getString("selectTheme"));
        layout.getChildren().add(themeLabel);

        themeGroup = new ToggleGroup();
        String currentTheme = SettingsManager.getInstance().getTheme();

        for (String theme : AVAILABLE_THEMES) {
            String localized = bundle.getString(switch (theme) {
                case "dark-mode" -> "themeDark";
                case "light-mode" -> "themeLight";
                case "protanopia" -> "themeProtanopia";
                case "deuteranopia" -> "themeDeuteranopia";
                case "tritanopia" -> "themeTritanopia";
                case "achromatopsia" -> "themeAchromatopsia";
                default -> theme;
            });
            RadioButton themeBtn = new RadioButton(localized);
            themeBtn.setUserData(theme);
            themeBtn.setToggleGroup(themeGroup);
            if (theme.equals(currentTheme)) themeBtn.setSelected(true);

            themeBtn.setOnAction(e -> {
                SettingsManager.getInstance().setTheme(theme);
                EventBus.getInstance().publish("themeChanged");
            });

            layout.getChildren().add(themeBtn);
        }
    }

    /**
     * Handles events published to the EventBus.
     * Updates the UI and stylesheet if the language or theme changes.
     *
     * @param eventType the type of event that occurred.
     */
    @Override
    public void onEvent(String eventType) {
        if (eventType.equals("languageChanged") || eventType.equals("themeChanged")) {
            ResourceBundle bundle = SettingsManager.getInstance().getBundle();
            String theme = SettingsManager.getInstance().getTheme();

            // Dynamically update labels and buttons text
            updateUI();

            // Dynamically update CSS
            scene.getStylesheets().clear();
            scene.getStylesheets().add(getClass().getResource("/qcs/themes/" + theme + ".css").toExternalForm());

            stage.setTitle(bundle.getString("settingsWindowTitle"));
        }
    }
}
