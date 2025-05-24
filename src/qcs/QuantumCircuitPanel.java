package qcs;
import qcs.EventBus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class QuantumCircuitPanel implements EventBus.EventListener {
    private VBox panel;
    private GridPane gridPane;
    private Button resizeButton;
    private Spinner<Integer> qubitsSpinner, stepsSpinner;
    private BottomPanel bottomPanel;
    private int qubits = 3, steps = 5;

    public QuantumCircuitPanel() {
        gridPane = new GridPane();
        gridPane.setGridLinesVisible(true);
        gridPane.setPadding(new Insets(10));
        drawGrid();

        qubitsSpinner = new Spinner<>(1, 10, qubits);
        stepsSpinner = new Spinner<>(1, 20, steps);
        resizeButton = new Button();
        resizeButton.setOnAction(e -> {
            qubits = qubitsSpinner.getValue();
            steps = stepsSpinner.getValue();
            drawGrid();
            if (bottomPanel != null) bottomPanel.updateStatus(SettingsManager.getInstance().getBundle().getString("resize"));
        });

        HBox controls = new HBox(10, new Label("Qubits:"), qubitsSpinner, new Label("Steps:"), stepsSpinner, resizeButton);
        controls.setAlignment(Pos.CENTER);

        panel = new VBox(10, gridPane, controls);
        panel.setAlignment(Pos.CENTER);
        updateUI();
        EventBus.getInstance().subscribe(this);
    }

    public void drawGrid() {
        gridPane.getChildren().clear();
        for (int r = 0; r < qubits; r++) {
            for (int c = 0; c < steps; c++) {
                Button cell = new Button("");
                cell.setPrefSize(80, 40);
                int finalR = r, finalC = c;
                cell.setOnAction(e -> {
                    cell.setText("H");
                    if (bottomPanel != null) bottomPanel.updateStatus("H placed at " + finalR + "," + finalC);
                });
                gridPane.add(cell, c, r);
            }
        }
    }

    public void updateUI() {
        resizeButton.setText(SettingsManager.getInstance().getBundle().getString("resize"));
    }

    public VBox getPanel() {
        return panel;
    }

    public void setBottomPanel(BottomPanel bp) {
        this.bottomPanel = bp;
    }

    @Override
    public void onEvent(String eventType) {
        if ("languageChanged".equals(eventType)) {
            updateUI();
        }
    }
}
