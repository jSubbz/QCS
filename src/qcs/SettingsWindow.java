package qcs;

import qcs.EventBus;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.ResourceBundle;

public class SettingsWindow {
    private static final List<String> AVAILABLE_LANGUAGES = List.of("en", "de", "fr");
    private static final List<String> AVAILABLE_THEME_CODES = List.of(
            "dark-mode", "light-mode", "protanopia", "deuteranopia", "tritanopia", "achromatopsia"
    );

    public void show() {
        Stage stage = new Stage();
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();
        stage.setTitle(bundle.getString("settingsWindowTitle"));

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        // 🔸 Language ComboBox (exclude current)
        Label languageLabel = new Label(bundle.getString("selectLanguage"));
        ComboBox<String> languageCombo = new ComboBox<>();
        String currentLanguage = SettingsManager.getInstance().getLanguage();
        AVAILABLE_LANGUAGES.stream()
                .filter(lang -> !lang.equals(currentLanguage))
                .forEach(languageCombo.getItems()::add);
        if (!languageCombo.getItems().isEmpty()) {
            languageCombo.setValue(languageCombo.getItems().get(0));
        }

        // 🔸 Language Apply Button
        Button applyLanguageButton = new Button(bundle.getString("apply"));
        applyLanguageButton.setOnAction(e -> {
            if (!languageCombo.getItems().isEmpty()) {
                SettingsManager.getInstance().setLanguage(languageCombo.getValue());
                EventBus.getInstance().publish("languageChanged");
            }
            stage.close();
        });

        // 🔸 Theme ComboBox (exclude current)
        Label themeLabel = new Label(bundle.getString("selectTheme"));
        ComboBox<String> themeCombo = new ComboBox<>();
        String currentTheme = SettingsManager.getInstance().getTheme();
        AVAILABLE_THEME_CODES.stream()
                .filter(themeCode -> !themeCode.equals(currentTheme))
                .map(themeCode -> switch (themeCode) {
                    case "dark-mode" -> bundle.getString("themeDark");
                    case "light-mode" -> bundle.getString("themeLight");
                    case "protanopia" -> bundle.getString("themeProtanopia");
                    case "deuteranopia" -> bundle.getString("themeDeuteranopia");
                    case "tritanopia" -> bundle.getString("themeTritanopia");
                    case "achromatopsia" -> bundle.getString("themeAchromatopsia");
                    default -> themeCode;
                })
                .forEach(themeCombo.getItems()::add);
        if (!themeCombo.getItems().isEmpty()) {
            themeCombo.setValue(themeCombo.getItems().get(0));
        }

        // 🔸 Theme Apply Button
        Button applyThemeButton = new Button(bundle.getString("apply"));
        applyThemeButton.setOnAction(e -> {
            if (!themeCombo.getItems().isEmpty()) {
                String selectedThemeLabel = themeCombo.getValue();
                String newThemeCode = AVAILABLE_THEME_CODES.stream()
                        .filter(code -> selectedThemeLabel.equals(switch (code) {
                            case "dark-mode" -> bundle.getString("themeDark");
                            case "light-mode" -> bundle.getString("themeLight");
                            case "protanopia" -> bundle.getString("themeProtanopia");
                            case "deuteranopia" -> bundle.getString("themeDeuteranopia");
                            case "tritanopia" -> bundle.getString("themeTritanopia");
                            case "achromatopsia" -> bundle.getString("themeAchromatopsia");
                            default -> code;
                        }))
                        .findFirst()
                        .orElse(currentTheme);
                SettingsManager.getInstance().setTheme(newThemeCode);
                EventBus.getInstance().publish("themeChanged");
            }
            stage.close();
        });

        // 🔸 Layout
        layout.getChildren().addAll(
                languageLabel, languageCombo, applyLanguageButton,
                themeLabel, themeCombo, applyThemeButton
        );

        Scene scene = new Scene(layout, 300, 300);
        scene.getStylesheets().add(getClass().getResource("themes/" + currentTheme + ".css").toExternalForm());
        layout.getStyleClass().add("settings-window");

        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
