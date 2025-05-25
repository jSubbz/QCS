package qcs;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import qcs.EventBus;


public class BottomPanel {

    private TextArea messageField;
    private Label messageBoxLabel;
    private Label tensorLabel;
    private TextField tensorTextField;
    private VBox mainBottomContainer;

    public BottomPanel() {

        //Construction of the message box contained in the bottom panel
        messageField = new TextArea();
        messageField.setEditable(false);
        messageField.setWrapText(true);

        //Construction of the subcontainer of the message box to allow for scrolling
        ScrollPane messageBoxPane = new ScrollPane();
        messageBoxPane.setPadding(new Insets(7.5));
        messageBoxPane.setContent(messageField);
        messageBoxPane.setFitToWidth(true);
        messageBoxPane.setFitToHeight(true);

        //Tensor Product Pane construction
        GridPane tensorProductPane = new GridPane();
        tensorLabel = new Label("Tensor Product: ");
        tensorTextField = new TextField("|0...0|>");//Just a default message for now
        tensorTextField.setEditable(false);
        tensorProductPane.add(tensorLabel, 0, 0);//Position of the label and text field
        tensorProductPane.add(tensorTextField, 1, 0);

        mainBottomContainer = new VBox(); //We use this to vertically stack the elements of the bottom panel
        mainBottomContainer.setPadding(new Insets(2.5));
        mainBottomContainer.setSpacing(2.5);
        mainBottomContainer.setStyle("-fx-padding: 5px");

        messageBoxLabel = new Label("Messages");

        mainBottomContainer.getChildren().addAll(tensorProductPane, messageBoxLabel, messageBoxPane);//Puts our child items into the main bottom container
        VBox.setVgrow(messageBoxPane, Priority.ALWAYS);//Allows textArea to grow and shrink with the window
    }

    /***
     * Returns the bottom panel
     * @return VBox containing the bottom panel
     */
    public VBox getPanel() {
        return mainBottomContainer;
    }

    /***
     * Outputs a new message to the bottom panel
     * Every message will be on a new line
     * @param message Message to be output to the screen
     */
    public void updateStatus(String message) {
        messageField.appendText(message + "\n");//Output message and a newline character
        messageField.setScrollTop(Double.MAX_VALUE);//Force scroll to the bottom on an update
    }
}
