package qcs;

import qcs.EventBus;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.io.*;

public class QuantumCircuitPanel implements EventBus.EventListener {
    private BorderPane panel;
    private GridPane gridPane;
    private Button resizeButton, newCircuitButton, stepButton, resetButton;
    private Spinner<Integer> qubitsSpinner, stepsSpinner;
    private BottomPanel bottomPanel;
    private int qubits = 3, steps = 5;
    private String selectedGate = null;
    private Button[][] cellButtons;  // Track buttons in grid

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
            if (bottomPanel != null)
                bottomPanel.updateStatus(bundle.getString("resize") + " to " + qubits + " qubits and " + steps + " steps.");
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

        cellButtons = new Button[qubits][steps];  // Initialize button tracker

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
                    if (bottomPanel != null)
                        bottomPanel.updateStatus((selectedGate != null ? selectedGate : "Cleared") + " at " + finalR + "," + finalC);
                });
                gridPane.add(cell, c, r);
                cellButtons[r][c] = cell;  // Store cell reference
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

    /** Checks if any cell is active (non-empty text). */
    public boolean hasActiveCells() {
        if (cellButtons == null) return false;
        for (int r = 0; r < qubits; r++) {
            for (int c = 0; c < steps; c++) {
                if (!cellButtons[r][c].getText().isEmpty()) return true;
            }
        }
        return false;
    }

    /** Clears all cells in the grid. */
    public void clearGrid() {
        if (cellButtons == null) return;
        for (int r = 0; r < qubits; r++) {
            for (int c = 0; c < steps; c++) {
                Button cell = cellButtons[r][c];
                cell.setText("");
                cell.getStyleClass().removeIf(style -> style.startsWith("gate-"));
                cell.setStyle("");
            }
        }
    }

    /** Saves the pattern to a file. */
    public void savePatternToFile(File file) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(qubits + "," + steps + "\n");  // Save dimensions
            for (int r = 0; r < qubits; r++) {
                for (int c = 0; c < steps; c++) {
                    String text = cellButtons[r][c].getText();
                    if (!text.isEmpty()) {
                        writer.write(r + "," + c + "," + text + "\n");
                    }
                }
            }
            System.out.println("Pattern saved to " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Loads a pattern from a file and updates the grid. */
    public void loadPatternFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine();
            String[] dims = line.split(",");
            int loadedQubits = Integer.parseInt(dims[0]);
            int loadedSteps = Integer.parseInt(dims[1]);

            qubits = loadedQubits;
            steps = loadedSteps;
            qubitsSpinner.getValueFactory().setValue(qubits);
            stepsSpinner.getValueFactory().setValue(steps);
            drawGrid();  // Rebuild grid with new size

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                int r = Integer.parseInt(parts[0]);
                int c = Integer.parseInt(parts[1]);
                String gate = parts[2];
                Button cell = cellButtons[r][c];
                cell.setText(gate);
                cell.getStyleClass().add("gate-" + gate);
            }
            System.out.println("Pattern loaded from " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
