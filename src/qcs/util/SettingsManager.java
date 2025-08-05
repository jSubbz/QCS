package qcs.util;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Properties;
import java.io.*;

/**
 * SettingsManager is a singleton class responsible for managing application settings such as language, theme, and mode.
 * It loads and saves settings from a properties file and publishes events when settings change.
 */
public class SettingsManager {
    private static SettingsManager instance;

    private String currentLanguage = "en";
    private String currentTheme = "dark-mode";
    private boolean designMode = true;

    private String username; // Stores currently logged-in username

    private static final String CONFIG_FILE = "settings.properties";
    private final Properties props = new Properties();

    /**
     * Private constructor to enforce the singleton pattern.
     * Loads settings from the configuration file.
     */
    private SettingsManager() {
        loadSettings();
    }

    /**
     * Returns the singleton instance of SettingsManager.
     *
     * @return the singleton SettingsManager instance.
     */
    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    // === Language Settings ===

    public String getLanguage() {
        return currentLanguage;
    }

    public void setLanguage(String lang) {
        currentLanguage = lang;
        props.setProperty("language", lang);
        saveSettings();
        EventBus.getInstance().publish("languageChanged");
    }

    // === Theme Settings ===

    public String getTheme() {
        return currentTheme;
    }

    public void setTheme(String theme) {
        currentTheme = theme;
        props.setProperty("theme", theme);
        saveSettings();
        EventBus.getInstance().publish("themeChanged");
    }

    // === Mode Handling ===

    public boolean isDesignMode() {
        return designMode;
    }

    public void toggleMode() {
        designMode = !designMode;
        EventBus.getInstance().publish("modeToggled");
    }

    public void setDesignMode(boolean mode) {
        this.designMode = mode;
    }

    // === Username / Session ===

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }

    // === Resource Bundle for Localization ===

    public ResourceBundle getBundle() {
        return ResourceBundle.getBundle("qcs.messages", new Locale(currentLanguage));
    }

    // === Persistence ===

    private void loadSettings() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            props.load(reader);
            currentLanguage = props.getProperty("language", "en");
            currentTheme = props.getProperty("theme", "dark-mode");
        } catch (IOException e) {
            System.out.println("Settings file not found, using defaults.");
        }
    }

    private void saveSettings() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            props.store(writer, "User Settings");
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }
}
