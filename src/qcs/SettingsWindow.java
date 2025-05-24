package qcs;
import qcs.EventBus;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class SettingsWindow {
    private static final List<String> AVAILABLE_LANGUAGES = List.of("en", "de", "fr");
    private static final List<String> AVAILABLE_THEMES = List.of("light-mode", "dark-mode", "protanopia", "deuteranopia", "tritanopia", "achromatopsia");

    public void show() {
        Stage stage = new Stage();
        stage.setTitle("Settings");

        VBox layout = new VBox(10);
        layout.setPadding(new Insets(10));

        ComboBox<String> languageCombo = new ComboBox<>();
        languageCombo.getItems().addAll(AVAILABLE_LANGUAGES);
        languageCombo.setValue(SettingsManager.getInstance().getLanguage());
        languageCombo.setOnAction(e -> {
            SettingsManager.getInstance().setLanguage(languageCombo.getValue());
            EventBus.getInstance().publish("languageChanged");
        });

        ComboBox<String> themeCombo = new ComboBox<>();
        themeCombo.getItems().addAll(AVAILABLE_THEMES);
        themeCombo.setValue(SettingsManager.getInstance().getTheme());
        themeCombo.setOnAction(e -> {
            SettingsManager.getInstance().setTheme(themeCombo.getValue());
            EventBus.getInstance().publish("themeChanged");
        });

        layout.getChildren().addAll(new Label("Select Language:"), languageCombo, new Label("Select Theme:"), themeCombo);

        Scene scene = new Scene(layout, 300, 200);
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }
}
