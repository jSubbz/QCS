package qcs.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import qcs.model.GateOperation;
import qcs.util.EventBus;
import qcs.util.SettingsManager;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import qcs.model.QuantumSimulator;
import qcs.model.Complex;


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
    private List<List<Complex>> simulatedStates = new ArrayList<>();

    private Timeline playTimeline;
    private Button simulateButton; // add if not already declared at the top
    private HBox actionButtons;  // add this as a field at top of class
    private Button modeToggleButton;

    private HBox designModeButtons;
    private HBox playModeButtons;
    private List<Button> highlightedCells = new ArrayList<>();



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

        Button toPlayModeButton = new Button("Go to Play Mode");
        toPlayModeButton.setOnAction(e -> {
            SettingsManager.getInstance().toggleMode();
            EventBus.getInstance().publish("modeToggled");
        });

        designModeButtons = new HBox(10, toPlayModeButton);
        designModeButtons.setAlignment(Pos.CENTER);

        newCircuitButton = new Button(bundle.getString("newCircuit"));
        newCircuitButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Start a new circuit? All progress will be lost.",
                    ButtonType.YES, ButtonType.NO);
            confirm.setTitle("New Circuit");
            var result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.YES) {
                stepOperations.clear();
                simulatedStates.clear();
                currentStep = 0;
                drawGrid();
                patternDisplayArea.clear();

                SettingsManager.getInstance().setDesignMode(true);
                EventBus.getInstance().publish("modeToggled");

                if (bottomPanel != null)
                    bottomPanel.updateStatus("Started a new circuit. Switched to design mode.");
            }
        });

        stepButton = new Button(bundle.getString("step"));
        stepButton.setOnAction(e -> {
            if (simulatedStates.isEmpty()) {
                bottomPanel.updateStatus("No simulation loaded. Switch to Play Mode first.");
                return;
            }

            if (currentStep < simulatedStates.size()) {
                renderStep(currentStep++);
            } else {
                bottomPanel.updateStatus("Reached end of simulation.");
            }
        });

        resetButton = new Button(bundle.getString("reset"));
        resetButton.setOnAction(e -> {
            currentStep = 0;
            patternDisplayArea.clear();
            bottomPanel.updateStatus("Simulation reset to step 0.");
        });

        simulateButton = new Button("Simulate");
        simulateButton.setOnAction(e -> {
            simulateCircuit();
            if (playTimeline != null && playTimeline.getStatus() == Timeline.Status.RUNNING) {
                playTimeline.stop();
            }

            currentStep = 0;
            playTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), event -> {
                if (currentStep < simulatedStates.size()) {
                    renderStep(currentStep++);
                } else {
                    playTimeline.stop();
                }
            }));
            playTimeline.setCycleCount(simulatedStates.size());
            playTimeline.play();
        });

        playModeButtons = new HBox(10, newCircuitButton, stepButton, resetButton, simulateButton);
        playModeButtons.setAlignment(Pos.CENTER);

        // Initial visibility
        boolean isDesign = SettingsManager.getInstance().isDesignMode();
        designModeButtons.setVisible(isDesign);
        playModeButtons.setVisible(!isDesign);

        HBox resizeControls = new HBox(10, new Label("Qubits:"), qubitsSpinner, new Label("Steps:"), stepsSpinner, resizeButton);
        resizeControls.setAlignment(Pos.CENTER);

        VBox bottomControls = new VBox(10, resizeControls, designModeButtons, playModeButtons);
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

        // ✅ Re-initialize stepOperations to correct dimensions
        stepOperations = new ArrayList<>();
        for (int i = 0; i < steps; i++) {
            stepOperations.add(new ArrayList<>());
        }

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
                    cell.getStyleClass().removeIf(style -> style.startsWith("gate-") || style.equals("invalid-gate"));

                    // ✅ Make sure selectedGate and mode are valid
                    if (!SettingsManager.getInstance().isDesignMode() || selectedGate == null || selectedGate.isEmpty()) {
                        cell.setText("");
                        if (bottomPanel != null)
                            bottomPanel.updateStatus("Cleared at " + finalR + "," + finalC);
                        return;
                    }

                    int stepIndex = finalC;
                    int qubit = finalR;

                    // 🧠 Determine if placement is valid and build targets
                    boolean valid = true;
                    int[] targets = null;

                    switch (selectedGate.toUpperCase()) {
                        case "CX", "CU" -> {
                            if (qubit + 1 >= qubits) valid = false;
                            else targets = new int[]{qubit, qubit + 1};
                        }
                        case "CCX" -> {
                            if (qubit + 2 >= qubits) valid = false;
                            else targets = new int[]{qubit, qubit + 1, qubit + 2};
                        }
                        case "SWAP" -> {
                            if (qubit + 1 >= qubits) valid = false;
                            else targets = new int[]{qubit, qubit + 1};
                        }
                        default -> {
                            targets = new int[]{qubit};
                        }
                    }

                    // ✅ Add operation only if valid
                    if (valid && targets != null) {
                        stepOperations.get(stepIndex).add(new GateOperation(selectedGate.toLowerCase(), targets));
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


    /**
     * Handles events published through the EventBus.
     *
     * @param eventType The type of event that occurred.
     */
    @Override
    public void onEvent(String eventType) {
        if (eventType.startsWith("gateSelected:")) {
            selectedGate = eventType.substring("gateSelected:".length());
        } else if ("languageChanged".equals(eventType)) {
            updateUI();
        } else if ("modeToggled".equals(eventType)) {
            boolean playMode = !SettingsManager.getInstance().isDesignMode();

            if (designModeButtons != null) designModeButtons.setVisible(!playMode);
            if (playModeButtons != null)  playModeButtons.setVisible(playMode);

            if (playMode) {
                simulateCircuit();
                if (bottomPanel != null)
                    bottomPanel.updateStatus("Switched to play mode. Simulation ready.");
            } else {
                currentStep = 0;
                if (bottomPanel != null)
                    bottomPanel.updateStatus("Switched to design mode.");
            }
        }
    }

    /**
     * Gets the circuit panel
     * @return panel
     */
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
        try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
            writer.println("OPENQASM 2.0;");
            writer.println("include \"qelib1.inc\";");
            writer.println("qreg q[" + qubits + "];");

            for (int i = 0; i < stepOperations.size(); i++) {
                writer.println("// STEP " + i);
                for (GateOperation op : stepOperations.get(i)) {
                    writer.print(op.gateName);
                    for (int j = 0; j < op.qubits.length; j++) {
                        writer.print(" q[" + op.qubits[j] + "]");
                        if (j < op.qubits.length - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println(";");
                }
            }

            bottomPanel.updateStatus("Saved circuit to: " + file.getName());
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

    /**
     * Set the bottom panel object
     * @param panel Panel to be set
     */
    public void setBottomPanel(BottomPanel panel) {
        this.bottomPanel = panel;
    }

    private void renderStep(int step) {
        if (step < 0 || step >= simulatedStates.size()) {
            bottomPanel.updateStatus("Step out of bounds.");
            return;
        }

        // 🔄 Clear previous highlights
        for (Button b : highlightedCells) {
            b.getStyleClass().remove("active-cell");
        }
        highlightedCells.clear();

        List<Complex> state = simulatedStates.get(step);
        patternDisplayArea.clear();
        patternDisplayArea.appendText("Step " + step + " state:\n");

        int numQubits = (int) (Math.log(state.size()) / Math.log(2));

        for (int i = 0; i < state.size(); i++) {
            Complex amp = state.get(i);
            String basis = String.format("|%s⟩",
                    String.format("%" + numQubits + "s", Integer.toBinaryString(i)).replace(' ', '0'));
            patternDisplayArea.appendText(basis + ": " + amp + "\n");
        }

        // 🔎 Highlight all qubits used in this step
        if (step < stepOperations.size()) {
            for (GateOperation op : stepOperations.get(step)) {
                for (int q : op.qubits) {
                    if (q >= 0 && q < qubits && step < steps) {
                        Button cell = cellButtons[q][step];
                        if (cell != null && !cell.getText().isEmpty()) {
                            cell.getStyleClass().add("active-cell");
                            highlightedCells.add(cell);
                        }
                    }
                }
            }
        }
        //update the graphic with new data
        EventBus.getInstance().publish("graphicUpdate");

        bottomPanel.updateStatus("Showing step " + step);
    }




    /**
     * Simulates the quantum circuit based on the defined {@code stepOperations}.
     * It clears any previous simulation results, initializes a new {@link QuantumSimulator},
     * applies gates step by step, and stores the quantum state after each step.
     */
    public void simulateCircuit() {
        simulatedStates.clear();
        QuantumSimulator sim = new QuantumSimulator(qubits);

        for (List<GateOperation> step : stepOperations) {
            for (GateOperation op : step) {
                sim.applyGate(op);
            }
            simulatedStates.add(sim.copyState());
        }

        currentStep = 0;
        if (bottomPanel != null) {
            bottomPanel.updateStatus("Simulation complete. Ready to step.");
        }
    }


    /**
     * Updates the text of the mode toggle button based on the current design mode status.
     * If in design mode, the button text will prompt to switch to play mode, and vice-versa.
     */
    private void updateModeToggleButton() {
        boolean isDesign = SettingsManager.getInstance().isDesignMode();
        modeToggleButton.setText(isDesign ? "Go to Play Mode" : "Go to Design Mode");
    }
}
