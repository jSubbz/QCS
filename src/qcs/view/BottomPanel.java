package qcs.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import qcs.util.EventBus;
import qcs.util.SettingsManager;

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
     * Constructs a BottomPanel object.
     * Initializes and builds the UI components, and subscribes to the EventBus for relevant events.
     */
    public BottomPanel() {
        EventBus.getInstance().subscribe(this);  // 🔥 Subscribe to listen for events like language change
        rebuildPanel();  // 🔥 Initial UI build
    }

    /**
     * Builds or rebuilds the bottom panel UI components using the latest ResourceBundle.
     * This method constructs the tensor product section and message box area.
     * It is called initially in the constructor and when a language change event occurs.
     */
    private void rebuildPanel() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();

        // Clear existing children
        if (mainBottomContainer == null) {
            mainBottomContainer = new VBox();
            mainBottomContainer.setPadding(new Insets(2.5));
            mainBottomContainer.setSpacing(2.5);
            mainBottomContainer.setStyle("-fx-padding: 5px");
        } else {
            mainBottomContainer.getChildren().clear();
        }

        // Tensor product section
        GridPane tensorProductPane = new GridPane();
        tensorLabel = new Label(bundle.getString("tensorLabel"));
        tensorTextField = new TextField(bundle.getString("defaultTensorText"));
        tensorTextField.setEditable(false);
        tensorProductPane.add(tensorLabel, 0, 0);
        tensorProductPane.add(tensorTextField, 1, 0);

        // Message box label
        messageBoxLabel = new Label(bundle.getString("messageBoxLabel"));

        // Message field inside scroll pane
        if (messageField == null) {
            messageField = new TextArea();
            messageField.setEditable(false);
            messageField.setWrapText(true);
        }

        ScrollPane messageBoxPane = new ScrollPane(messageField);
        messageBoxPane.setPadding(new Insets(7.5));
        messageBoxPane.setFitToWidth(true);
        messageBoxPane.setFitToHeight(true);
        messageBoxPane.setPrefWidth(500);

// Allow scroll pane to stretch
        HBox.setHgrow(messageBoxPane, Priority.ALWAYS);
        messageBoxPane.setMaxWidth(Double.MAX_VALUE);

// Quantum simulation image
        ImageView quantumImage = new ImageView(getClass().getResource("../quantum_states.jpg").toExternalForm());
        quantumImage.setPreserveRatio(true);
        quantumImage.setFitWidth(200);
        quantumImage.setSmooth(true);

// Combined layout
        HBox messageAndImageRow = new HBox(10, messageBoxPane, quantumImage);
        messageAndImageRow.setAlignment(Pos.CENTER);
        messageAndImageRow.setPadding(new Insets(5));
        messageAndImageRow.setFillHeight(true);

// Add all to bottom container
        mainBottomContainer.getChildren().addAll(
                tensorProductPane,
                messageBoxLabel,
                messageAndImageRow
        );
        VBox.setVgrow(messageAndImageRow, Priority.ALWAYS);
    }


    /**
     * Returns the VBox container holding the bottom panel UI components.
     *
     * @return VBox containing the bottom panel UI.
     */
    public VBox getPanel() {
        return mainBottomContainer;
    }

    /**
     * Updates the message box with a new message.
     * Appends the message to the TextArea and scrolls to the bottom.
     *
     * @param message The message to display in the message box.
     */
    public void updateStatus(String message) {
        messageField.appendText(message + "\n");
        messageField.setScrollTop(Double.MAX_VALUE);
    }

    /**
     * Handles events published to the EventBus.
     * Rebuilds the UI when a "languageChanged" event is detected.
     *
     * @param eventType The type of event that occurred.
     */
    @Override
    public void onEvent(String eventType) {
        if ("languageChanged".equals(eventType)) {
            rebuildPanel();  // 🔥 Rebuild UI with new language settings
        }
    }
}
