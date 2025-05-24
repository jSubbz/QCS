package qcs;
import qcs.EventBus;
import javafx.scene.control.TextField;

public class BottomPanel {
    private TextField statusField;

    public BottomPanel() {
        statusField = new TextField();
        statusField.setEditable(false);
    }

    public TextField getPanel() {
        return statusField;
    }

    public void updateStatus(String message) {
        statusField.setText(message);
    }
}
