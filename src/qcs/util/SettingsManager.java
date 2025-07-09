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

    /**
     * Gets the current language setting.
     *
     * @return the current language code (e.g., "en", "de").
     */
    public String getLanguage() {
        return currentLanguage;
    }

    /**
     * Sets the current language and saves the change.
     * Publishes a "languageChanged" event.
     *
     * @param lang the new language code.
     */
    public void setLanguage(String lang) {
        currentLanguage = lang;
        props.setProperty("language", lang);
        saveSettings();
        EventBus.getInstance().publish("languageChanged");
    }

    /**
     * Gets the current theme setting.
     *
     * @return the current theme name.
     */
    public String getTheme() {
        return currentTheme;
    }

    /**
     * Sets the current theme and saves the change.
     * Publishes a "themeChanged" event.
     *
     * @param theme the new theme name.
     */
    public void setTheme(String theme) {
        currentTheme = theme;
        props.setProperty("theme", theme);
        saveSettings();
        EventBus.getInstance().publish("themeChanged");
    }

    /**
     * Checks if the application is in design mode.
     *
     * @return true if in design mode, false if in play mode.
     */
    public boolean isDesignMode() {
        return designMode;
    }

    /**
     * Toggles between design and play modes.
     * Publishes a "modeToggled" event.
     */
    public void toggleMode() {
        designMode = !designMode;
        EventBus.getInstance().publish("modeToggled");
    }

    /**
     * Retrieves the ResourceBundle for the current language.
     *
     * @return the ResourceBundle containing localized strings.
     */
    public ResourceBundle getBundle() {
        return ResourceBundle.getBundle("qcs.messages", new Locale(currentLanguage));
    }

    /**
     * Loads settings from the configuration file. If the file doesn't exist, default values are used.
     */
    private void loadSettings() {
        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            props.load(reader);
            currentLanguage = props.getProperty("language", "en");
            currentTheme = props.getProperty("theme", "dark-mode");
        } catch (IOException e) {
            System.out.println("Settings file not found, using defaults.");
        }
    }

    /**
     * Saves the current settings to the configuration file.
     */
    private void saveSettings() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            props.store(writer, "User Settings");
        } catch (IOException e) {
            System.err.println("Error saving settings: " + e.getMessage());
        }
    }
    public void setDesignMode(boolean mode) {
        this.designMode = mode;
    }

}
