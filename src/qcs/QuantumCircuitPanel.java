package qcs;

import qcs.EventBus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class QuantumCircuitPanel implements EventBus.EventListener {
    private BorderPane panel;
    private GridPane gridPane;
    private Button resizeButton, newCircuitButton, stepButton, resetButton;
    private Spinner<Integer> qubitsSpinner, stepsSpinner;
    private BottomPanel bottomPanel;
    private int qubits = 3, steps = 5;
    private String selectedGate = null;

    public QuantumCircuitPanel() {
        var bundle = SettingsManager.getInstance().getBundle();

        // Create the label and grid
        Label gridLabel = new Label("Quantum Circuit");
        gridLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        gridPane = new GridPane();
        gridPane.setGridLinesVisible(true);
        gridPane.setPadding(new Insets(10));
        gridPane.setStyle("-fx-background-color: #FFFFFF;");
        GridPane.setHgrow(gridPane, Priority.ALWAYS);
        GridPane.setVgrow(gridPane, Priority.ALWAYS);
        gridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        BorderPane gridContainer = new BorderPane();
        gridContainer.setTop(gridLabel);
        BorderPane.setAlignment(gridLabel, Pos.CENTER);
        gridContainer.setCenter(gridPane);

        // Resize controls with localization
        qubitsSpinner = new Spinner<>(1, 10, qubits);
        stepsSpinner = new Spinner<>(1, 20, steps);
        resizeButton = new Button(bundle.getString("resize"));
        resizeButton.setOnAction(e -> {
            qubits = qubitsSpinner.getValue();
            steps = stepsSpinner.getValue();
            drawGrid();
            if (bottomPanel != null) bottomPanel.updateStatus(bundle.getString("resize") + " to " + qubits + " qubits and " + steps + " steps.");
        });
        HBox resizeControls = new HBox(10, new Label("Qubits:"), qubitsSpinner, new Label("Steps:"), stepsSpinner, resizeButton);
        resizeControls.setAlignment(Pos.CENTER);
        resizeControls.setPadding(new Insets(10));

        // Additional action buttons with localization
        newCircuitButton = new Button(bundle.getString("newCircuit"));
        stepButton = new Button(bundle.getString("step"));
        resetButton = new Button(bundle.getString("reset"));
        HBox actionButtons = new HBox(10, newCircuitButton, stepButton, resetButton);
        actionButtons.setAlignment(Pos.CENTER);
        actionButtons.setPadding(new Insets(10));

        VBox bottomControls = new VBox(10, resizeControls, actionButtons);
        bottomControls.setAlignment(Pos.CENTER);

        panel = new BorderPane();
        panel.setCenter(gridContainer);
        panel.setBottom(bottomControls);
        panel.setPadding(new Insets(10));
        BorderPane.setAlignment(bottomControls, Pos.CENTER);

        updateUI();
        EventBus.getInstance().subscribe(this);

        drawGrid();
    }

    public void drawGrid() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        for (int c = 0; c < steps; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / steps);
            cc.setHgrow(Priority.ALWAYS);
            gridPane.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < qubits; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / qubits);
            rc.setVgrow(Priority.ALWAYS);
            gridPane.getRowConstraints().add(rc);
        }

        for (int r = 0; r < qubits; r++) {
            for (int c = 0; c < steps; c++) {
                Button cell = new Button("");
                cell.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                cell.getStyleClass().add("grid-cell");
                int finalR = r, finalC = c;
                cell.setOnAction(e -> {
                    cell.getStyleClass().removeIf(style -> style.startsWith("gate-"));
                    cell.setStyle("");
                    if (selectedGate != null) {
                        cell.setText(selectedGate);
                        cell.getStyleClass().add("gate-" + selectedGate);
                    } else {
                        cell.setText("");
                    }
                    if (bottomPanel != null) bottomPanel.updateStatus((selectedGate != null ? selectedGate : "Cleared") + " at " + finalR + "," + finalC);
                });
                gridPane.add(cell, c, r);
            }
        }
    }

    public void updateUI() {
        var bundle = SettingsManager.getInstance().getBundle();
        resizeButton.setText(bundle.getString("resize"));
        newCircuitButton.setText(bundle.getString("newCircuit"));
        stepButton.setText(bundle.getString("step"));
        resetButton.setText(bundle.getString("reset"));
    }

    public BorderPane getPanel() {
        return panel;
    }

    public void setBottomPanel(BottomPanel bp) {
        this.bottomPanel = bp;
    }

    @Override
    public void onEvent(String eventType) {
        if (eventType.startsWith("gateSelected:")) {
            selectedGate = eventType.substring("gateSelected:".length());
        } else if ("languageChanged".equals(eventType)) {
            updateUI();
        }
    }
}
