package qcs;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

/**
 * BottomPanel constructs the bottom panel of the UI, including a message box and tensor product display.
 * It integrates localization support using SettingsManager's resource bundle and listens for language changes.
 */
public class BottomPanel implements EventBus.EventListener {

    private TextArea messageField;
    private Label messageBoxLabel;
    private Label tensorLabel;
    private TextField tensorTextField;
    private VBox mainBottomContainer;

    /**
     * Constructor to initialize and build the BottomPanel UI components.
     */
    public BottomPanel() {
        EventBus.getInstance().subscribe(this);  // 🔥 Subscribe to listen for events like language change
        rebuildPanel();  // 🔥 Initial UI build
    }

    /**
     * Builds or rebuilds the bottom panel UI using the latest ResourceBundle.
     */
    private void rebuildPanel() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();

        // Clear existing children instead of replacing the container
        if (mainBottomContainer == null) {
            mainBottomContainer = new VBox();
            mainBottomContainer.setPadding(new Insets(2.5));
            mainBottomContainer.setSpacing(2.5);
            mainBottomContainer.setStyle("-fx-padding: 5px");
        } else {
            mainBottomContainer.getChildren().clear();
        }

        // Update tensor section
        GridPane tensorProductPane = new GridPane();
        tensorLabel = new Label(bundle.getString("tensorLabel"));
        tensorTextField = new TextField(bundle.getString("defaultTensorText"));
        tensorTextField.setEditable(false);
        tensorProductPane.add(tensorLabel, 0, 0);
        tensorProductPane.add(tensorTextField, 1, 0);

        // Message box label
        messageBoxLabel = new Label(bundle.getString("messageBoxLabel"));

        // Scroll pane
        if (messageField == null) {
            messageField = new TextArea();
            messageField.setEditable(false);
            messageField.setWrapText(true);
        }
        ScrollPane messageBoxPane = new ScrollPane(messageField);
        messageBoxPane.setPadding(new Insets(7.5));
        messageBoxPane.setFitToWidth(true);
        messageBoxPane.setFitToHeight(true);

        // Add components
        mainBottomContainer.getChildren().addAll(tensorProductPane, messageBoxLabel, messageBoxPane);
        VBox.setVgrow(messageBoxPane, Priority.ALWAYS);
    }

    /**
     * Returns the VBox containing the bottom panel.
     * @return VBox with the UI components.
     */
    public VBox getPanel() {
        return mainBottomContainer;
    }

    /**
     * Updates the message box with a new message.
     * @param message The message to display.
     */
    public void updateStatus(String message) {
        messageField.appendText(message + "\n");
        messageField.setScrollTop(Double.MAX_VALUE);
    }

    /**
     * Event listener to handle relevant events (like language change).
     * @param eventType The type of event.
     */
    @Override
    public void onEvent(String eventType) {
        if ("languageChanged".equals(eventType)) {
            rebuildPanel();  // 🔥 Rebuild UI with new language settings
        }
    }
}
