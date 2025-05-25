package qcs;
import qcs.EventBus;
import java.util.Locale;
import java.util.ResourceBundle;

public class SettingsManager {
    private static SettingsManager instance;
    private String currentLanguage = "en";
    private String currentTheme = "dark-mode";
    private boolean designMode = true;

    private SettingsManager() {}

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
        }
        return instance;
    }

    public String getLanguage() {
        return currentLanguage;
    }

    public void setLanguage(String lang) {
        currentLanguage = lang;
        EventBus.getInstance().publish("languageChanged");  // 🔥 Notify UI components
    }

    public String getTheme() {
        return currentTheme;
    }

    public void setTheme(String theme) {
        currentTheme = theme;
    }

    public boolean isDesignMode() {
        return designMode;
    }

    public void toggleMode() {
        designMode = !designMode;
    }

    public ResourceBundle getBundle() {
        return ResourceBundle.getBundle("qcs.messages", new Locale(currentLanguage));
    }
}
