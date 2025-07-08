package qcs.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import qcs.model.GateOperation;
import qcs.model.QasmExporter;
import qcs.util.EventBus;
import qcs.util.SettingsManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * QuantumCircuitPanel represents the grid area for displaying and managing quantum circuits.
 * It includes a resizable grid, pattern display, and interactive controls.
 */
public class QuantumCircuitPanel implements EventBus.EventListener {

    private BorderPane panel;
    private GridPane gridPane;
    private Button resizeButton, newCircuitButton, stepButton, resetButton;
    private Spinner<Integer> qubitsSpinner, stepsSpinner;
    private BottomPanel bottomPanel;

    private int qubits = 3;
    private int steps = 5;
    private String selectedGate = null;
    private Button[][] cellButtons;
    private TextArea patternDisplayArea;

    // Step simulation state
    private int currentStep = 0;
    private List<List<GateOperation>> stepOperations = new ArrayList<>();
    private List<List<Double>> simulatedStates = new ArrayList<>();


    /**
     * Constructs QuantumCircuitPanel, initializing the grid, controls, and subscribing to events.
     */
    public QuantumCircuitPanel() {
        initializePanel();
        initializeControls();
        initializePatternDisplayArea();
        drawGrid();
        updateUI();

        EventBus.getInstance().subscribe(this);
    }

    /**
     * Initializes the main UI layout container, including grid setup.
     */
    private void initializePanel() {
        panel = new BorderPane();

        Label gridLabel = new Label("Quantum Circuit");
        gridLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        gridPane = new GridPane();
        gridPane.setGridLinesVisible(true);
        gridPane.setPadding(new Insets(10));
        gridPane.setStyle("-fx-background-color: #FFFFFF;");
        gridPane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        BorderPane gridContainer = new BorderPane();
        gridContainer.setTop(gridLabel);
        BorderPane.setAlignment(gridLabel, Pos.CENTER);
        gridContainer.setCenter(gridPane);

        panel.setCenter(gridContainer);
        panel.setPadding(new Insets(10));
    }

    /**
     * Initializes the area for displaying loaded patterns or logging interactions.
     */
    private void initializePatternDisplayArea() {
        patternDisplayArea = new TextArea();
        patternDisplayArea.setEditable(false);
        patternDisplayArea.setWrapText(true);
        patternDisplayArea.setPrefWidth(250);

        panel.setRight(patternDisplayArea);
    }

    /**
     * Initializes UI controls like buttons and spinners for interaction and resizing the grid.
     */
    private void initializeControls() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();

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

        newCircuitButton = new Button(bundle.getString("newCircuit"));
        stepButton = new Button(bundle.getString("step"));
        resetButton = new Button(bundle.getString("reset"));

        HBox resizeControls = new HBox(10, new Label("Qubits:"), qubitsSpinner, new Label("Steps:"), stepsSpinner, resizeButton);
        resizeControls.setAlignment(Pos.CENTER);

        HBox actionButtons = new HBox(10, newCircuitButton, stepButton, resetButton);
        actionButtons.setAlignment(Pos.CENTER);

        VBox bottomControls = new VBox(10, resizeControls, actionButtons);
        bottomControls.setAlignment(Pos.CENTER);

        panel.setBottom(bottomControls);
    }

    /**
     * Draws the interactive quantum circuit grid, with event handlers for cell interaction.
     */
    /**
     * Draws the interactive quantum circuit grid, with event handlers for cell interaction.
     * Supports placing gates, validating qubit requirements, and building stepOperations for QASM export.
     */
    public void drawGrid() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        cellButtons = new Button[qubits][steps];
        stepOperations.clear(); // clear logic with new grid

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
                    // Clear old styles
                    cell.getStyleClass().removeIf(style -> style.startsWith("gate-") || style.equals("invalid-gate"));

                    // Do nothing if no gate selected or not in design mode
                    if (!SettingsManager.getInstance().isDesignMode() || selectedGate == null || selectedGate.isEmpty()) {
                        cell.setText("");
                        if (bottomPanel != null)
                            bottomPanel.updateStatus("Cleared at " + finalR + "," + finalC);
                        return;
                    }

                    int stepIndex = finalC;
                    int qubit = finalR;

                    // Ensure we have a list for this step
                    while (stepOperations.size() <= stepIndex) {
                        stepOperations.add(new ArrayList<>());
                    }

                    // Logic for gate validity
                    boolean valid = true;
                    int[] targets;

                    switch (selectedGate.toUpperCase()) {
                        case "CX":
                        case "CU":
                            if (qubit + 1 >= qubits) {
                                valid = false;
                            } else {
                                targets = new int[]{qubit, qubit + 1};
                                stepOperations.get(stepIndex).add(new GateOperation(selectedGate.toLowerCase(), targets));
                            }
                            break;

                        case "CCX":
                            if (qubit + 2 >= qubits) {
                                valid = false;
                            } else {
                                targets = new int[]{qubit, qubit + 1, qubit + 2};
                                stepOperations.get(stepIndex).add(new GateOperation(selectedGate.toLowerCase(), targets));
                            }
                            break;

                        default:
                            // Single qubit gate
                            targets = new int[]{qubit};
                            stepOperations.get(stepIndex).add(new GateOperation(selectedGate.toLowerCase(), targets));
                            break;
                    }

                    if (valid) {
                        cell.setText(selectedGate);
                        cell.getStyleClass().add("gate-" + selectedGate);
                        if (bottomPanel != null)
                            bottomPanel.updateStatus("Placed " + selectedGate + " at " + qubit + "," + stepIndex);
                    } else {
                        cell.setText("❌");
                        cell.getStyleClass().add("invalid-gate");
                        if (bottomPanel != null)
                            bottomPanel.updateStatus("Invalid placement of " + selectedGate + " at row " + qubit);
                    }
                });

                gridPane.add(cell, c, r);
                cellButtons[r][c] = cell;
            }
        }
    }


    /**
     * Updates UI components to reflect current language and theme settings.
     */
    public void updateUI() {
        ResourceBundle bundle = SettingsManager.getInstance().getBundle();

        resizeButton.setText(bundle.getString("resize"));
        newCircuitButton.setText(bundle.getString("newCircuit"));
        stepButton.setText(bundle.getString("step"));
        resetButton.setText(bundle.getString("reset"));
    }

    @Override
    public void onEvent(String eventType) {
        if (eventType.startsWith("gateSelected:")) {
            selectedGate = eventType.substring("gateSelected:".length());
        } else if ("languageChanged".equals(eventType)) {
            updateUI();
        }
    }

    public BorderPane getPanel() {
        return panel;
    }
    /**
     * Checks if any cell in the grid contains text, indicating an active cell.
     *
     * @return true if there is an active cell, false otherwise.
     */
    public boolean hasActiveCells() {
        if (cellButtons == null) return false;
        for (int r = 0; r < qubits; r++) {
            for (int c = 0; c < steps; c++) {
                if (!cellButtons[r][c].getText().isEmpty()) return true;
            }
        }
        return false;
    }

    /**
     * Clears all cells in the grid.
     */
    public void clearGrid() {
        if (cellButtons == null) return;
        for (int r = 0; r < qubits; r++) {
            for (int c = 0; c < steps; c++) {
                Button cell = cellButtons[r][c];
                cell.setText("");
                cell.getStyleClass().removeIf(style -> style.startsWith("gate-"));
            }
        }
        patternDisplayArea.clear(); // 🔥 Also clear the pattern log
    }

    /**
     * Saves the current grid pattern to a file.
     *
     * @param file File to save the pattern to.
     */
    public void savePatternToFile(File file) {
        try {
            QasmExporter.writeQasm(file, qubits, stepOperations); // stepOperations is List<List<GateOperation>>
            bottomPanel.updateStatus("Saved QASM to: " + file.getName());
        } catch (IOException e) {
            bottomPanel.updateStatus("Failed to save: " + e.getMessage());
        }
    }


    /**
     * Loads a grid pattern from a file.
     *
     * @param file File to load the pattern from.
     */
    public void loadPatternFromFile(File file) {
        stepOperations.clear();
        clearGrid();  // reset board

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int currentStep = -1;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                if (line.startsWith("qreg")) {
                    int start = line.indexOf('[') + 1;
                    int end = line.indexOf(']');
                    qubits = Integer.parseInt(line.substring(start, end));
                    qubitsSpinner.getValueFactory().setValue(qubits);
                    drawGrid();
                } else if (line.startsWith("// STEP")) {
                    currentStep++;
                    stepOperations.add(new ArrayList<>());
                } else if (line.contains("q[")) {
                    String[] parts = line.replace(";", "").split("\\s+");
                    String gate = parts[0];
                    String[] qargs = parts[1].split(",");
                    int[] qubitIndices = new int[qargs.length];
                    for (int i = 0; i < qargs.length; i++) {
                        int idxStart = qargs[i].indexOf('[') + 1;
                        int idxEnd = qargs[i].indexOf(']');
                        qubitIndices[i] = Integer.parseInt(qargs[i].substring(idxStart, idxEnd));
                    }

                    // Add to step list
                    stepOperations.get(currentStep).add(new GateOperation(gate, qubitIndices));

                    // Place visually only first qubit (you can improve this later)
                    int row = qubitIndices[0];
                    int col = currentStep;
                    Button cell = cellButtons[row][col];
                    cell.setText(gate.toUpperCase());
                    cell.getStyleClass().add("gate-" + gate.toUpperCase());
                }
            }

            patternDisplayArea.setText("Loaded QASM: " + file.getName());
        } catch (IOException e) {
            patternDisplayArea.setText("Failed to load: " + e.getMessage());
        }
    }
}
