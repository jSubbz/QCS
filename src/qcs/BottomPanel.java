package qcs;
import javafx.geometry.Insets;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

public class BottomPanel {
    private TextArea statusField;
    private ScrollPane messageBoxPane;
    public BottomPanel() {
         messageBoxPane = new ScrollPane();
        messageBoxPane.setPadding(new Insets(5.0));
        statusField = new TextArea();
        statusField.setEditable(false);
        statusField.setWrapText(true);
        messageBoxPane.setContent(statusField);
        messageBoxPane.setFitToWidth(true);
        messageBoxPane.setFitToHeight(true);
    }

    public ScrollPane getPanel() {
        return messageBoxPane;
    }

    public void updateStatus(String message) {
        statusField.setText(message + "\n");
    }
}
