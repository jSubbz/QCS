package qcs.view;

import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import qcs.util.EventBus;
import qcs.util.SettingsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
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
    private BarChart<String, Number> probabilityChart;
    private XYChart.Series<String, Number> probabilitySeries;
    private final ArrayList<String> labelArray = new ArrayList<>(Arrays.asList("0000","0001", "0010", "0011", "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011", "1100", "1101", "1110", "1111"));

    /**
     * Constructs a BottomPanel object.
     * Initializes and builds the UI components, and subscribes to the EventBus for relevant events.
     */
    public BottomPanel() {
        EventBus.getInstance().subscribe(this);  // 🔥 Subscribe to listen for events like language change
        setupChart(); // Sets up chart for display
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

        // Combined layout
        HBox messageAndImageRow = new HBox(10, messageBoxPane, probabilityChart);
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
     * Generates a list of 16 values that add up to a 100% to represent the probabilities of the simulation
     * @return List containing new generated values for the probabilities list
     */
    private ArrayList<Integer> generateProbabilities() {
        ArrayList<Integer> probabilities = new ArrayList<>(16);
        Random randomGenerator = new Random();
        int currentSum = 0;
        int fullSum = 100;
        int numProbabilities = 16;

        for (int i = 0; i < numProbabilities - 1; i++) {
            int remainingSum = fullSum - currentSum;
            int remainingProbabilities = numProbabilities - i;

            // Calculate min and max possible value for the current probability
            int min = Math.max(0, remainingSum - (remainingProbabilities - 1) * 100); // Ensure remaining can be 100
            int max = Math.min(100, remainingSum); // Ensure we don't exceed remaining sum

            int value = randomGenerator.nextInt(max - min + 1) + min;
            probabilities.add(value);
            currentSum += value;
        }
        probabilities.add(fullSum - currentSum); // The last probability takes the remaining sum

        return probabilities;
    }

    /**
     * Sets up the BarChart for displaying probabilities. Initializes the axes, chart, and series with default zero values.
     */
    private void setupChart(){
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setCategories(FXCollections.<String>observableArrayList(labelArray));
        NumberAxis yAxis = new NumberAxis();
        probabilityChart = new BarChart<>(xAxis, yAxis);
        probabilitySeries = new XYChart.Series<>();
        for(int i = 0; i < 16; i++)
            probabilitySeries.getData().add(new XYChart.Data<>(labelArray.get(i), 0));
        probabilityChart.getData().add(probabilitySeries);
    }

    /**
     * Updates the BarChart with new probability values.
     * @param probabilities An ArrayList of integers representing the new probability values for each category.
     */
    private void updateChart(ArrayList<Integer> probabilities){
        for(int i = 0; i < 16; i++) {
            probabilitySeries.getData().get(i).setYValue(probabilities.get(i));
        }
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
     * Updates the barChart when a "graphicUpdate" event is detected.
     * @param eventType The type of event that occurred.
     */
    @Override
    public void onEvent(String eventType) {
        if ("languageChanged".equals(eventType)) {
            rebuildPanel();  // 🔥 Rebuild UI with new language settings
        }
        else if("graphicUpdate".equals(eventType)){
            updateChart(generateProbabilities());
        }
    }
}
