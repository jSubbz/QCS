package qcs.view;
import qcs.network.*;

import java.net.Socket;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import qcs.network.ClientRequest;
import qcs.network.RequestType;
import qcs.network.ServerResponse;
import qcs.util.SettingsManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import qcs.model.GateOperation;
import qcs.util.EventBus;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import qcs.model.QuantumSimulator;
import qcs.model.Complex;

import static qcs.network.Client.SERVER_HOST;
import static qcs.network.Client.SERVER_PORT;


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
        stepsSpinner  = new Spinner<>(1, 20, steps);

        resizeButton = new Button(bundle.getString("resize"));
        resizeButton.setOnAction(e -> {
            qubits = qubitsSpinner.getValue();
            steps  = stepsSpinner.getValue();
            drawGrid();
            if (bottomPanel != null) {
                bottomPanel.updateStatus(bundle.getString("resize") + " to " + qubits + " qubits and " + steps + " steps.");
            }
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
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Start a new circuit? All progress will be lost.",
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
                if (bottomPanel != null) bottomPanel.updateStatus("No simulation loaded. Switch to Play Mode first.");
                return;
            }
            if (currentStep < simulatedStates.size()) {
                renderStep(currentStep++);
            } else {
                if (bottomPanel != null) bottomPanel.updateStatus("Reached end of simulation.");
            }
        });

        resetButton = new Button(bundle.getString("reset"));
        resetButton.setOnAction(e -> {
            currentStep = 0;
            patternDisplayArea.clear();
            if (bottomPanel != null) bottomPanel.updateStatus("Simulation reset to step 0.");
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

        // --- Save to DB ---
        Button saveButton = new Button("Save to DB");
        saveButton.setOnAction(e -> {
            String username = SettingsManager.getInstance().getUsername();
            if (username == null || username.isEmpty()) {
                showAlert("Cannot save: Not logged in.");
                return;
            }
            String json = exportToJson();
            CircuitDataRequest saveRequest = new CircuitDataRequest(username, json);
            ServerResponse response = NetworkClient.getInstance().sendRequest(saveRequest);

            if (response != null && response.isSuccess()) {
                showAlert("Circuit saved to DB successfully.");
            } else {
                String errorMsg = (response != null) ? response.getMessage() : "No response from server.";
                showAlert("Failed to save circuit. " + errorMsg);
            }
        });

        // --- Load from DB (pretty labels) ---
        Button loadButton = new Button("Load from DB");
        loadButton.setOnAction(e -> {
            ClientRequest loadRequest = new ClientRequest(RequestType.LOAD_CIRCUIT);
            loadRequest.setUsername(SettingsManager.getInstance().getUsername());

            ServerResponse response = NetworkClient.getInstance().sendRequest(loadRequest);

            if (response != null && response.isSuccess()) {
                java.util.List<String> circuits = response.getCircuits();
                if (circuits == null || circuits.isEmpty()) {
                    showAlert("No saved circuits found.");
                    return;
                }

                // Build readable labels and keep a reversible map
                java.util.Map<String, String> labelToJson = new java.util.LinkedHashMap<>();
                for (String json : circuits) {
                    String label = summarizeCircuitJson(json); // helper method added earlier
                    // ensure uniqueness in case summaries collide
                    String unique = label;
                    int n = 2;
                    while (labelToJson.containsKey(unique)) {
                        unique = label + "  #" + (n++);
                    }
                    labelToJson.put(unique, json);
                }

                ChoiceDialog<String> dialog = new ChoiceDialog<>(
                        labelToJson.keySet().iterator().next(),
                        labelToJson.keySet()
                );
                dialog.setTitle("Load Circuit");
                dialog.setHeaderText("Select a saved circuit to load:");
                dialog.setContentText("Circuit:");

                dialog.showAndWait().ifPresent(selectedLabel -> {
                    String selectedJson = labelToJson.get(selectedLabel);
                    loadPatternFromJson(selectedJson);
                    if (bottomPanel != null) bottomPanel.updateStatus("Loaded circuit from DB.");
                });

            } else {
                showAlert("Failed to load circuits from server.");
            }
        });

        // Layout groups
        HBox dbButtons = new HBox(10, saveButton, loadButton);
        dbButtons.setAlignment(Pos.CENTER);

        playModeButtons = new HBox(10, newCircuitButton, stepButton, resetButton, simulateButton);
        playModeButtons.setAlignment(Pos.CENTER);

        boolean isDesign = SettingsManager.getInstance().isDesignMode();
        designModeButtons.setVisible(isDesign);
        playModeButtons.setVisible(!isDesign);

        HBox resizeControls = new HBox(10,
                new Label("Qubits:"), qubitsSpinner,
                new Label("Steps:"),  stepsSpinner,
                resizeButton);
        resizeControls.setAlignment(Pos.CENTER);

        VBox bottomControls = new VBox(10, resizeControls, dbButtons, designModeButtons, playModeButtons);
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

                    if(!SettingsManager.getInstance().isDesignMode())
                        return;

                    cell.getStyleClass().removeIf(style -> style.startsWith("gate-") || style.equals("invalid-gate"));



                    // ✅ Make sure selectedGate and mode are valid
                    if (SettingsManager.getInstance().isDesignMode() && (selectedGate == null || selectedGate.isEmpty())) {
                        cell.setText("");
                        cell.getStyleClass().add("grid-cell");
                        cell.setStyle(null);//clears inline styles
                        stepOperations.get(finalC).removeIf(op -> op.qubits[0] == finalR);//remove from sim
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

                cell.setOnMouseClicked(event -> {
                    if (event.getButton() == MouseButton.SECONDARY) {
                        // Only proceed if the cell actually has a gate.
                        if (cell.getText().isEmpty() || !SettingsManager.getInstance().isDesignMode()) {
                            return;
                        }

                        final ContextMenu contextMenu = new ContextMenu();

                        // --- 1. "Clear Gate" Option ---
                        MenuItem clearGateMenuItem = new MenuItem("Clear Gate");
                        clearGateMenuItem.setOnAction(e -> {
                            // First, remove the gate from the underlying data model.
                            stepOperations.get(finalC).removeIf(op -> op.qubits[0] == finalR);

                            // Then, update the UI to reflect the removal.
                            cell.setText("");
                            cell.getStyleClass().removeIf(style -> style.startsWith("gate-") || style.equals("invalid-gate"));
                            cell.setStyle(null); // This removes the inline background color.

                            if (bottomPanel != null) {
                                bottomPanel.updateStatus("Cleared gate at " + finalR + "," + finalC);
                            }
                        });
                        contextMenu.getItems().add(clearGateMenuItem);

                        // --- 2. "Set Color" Option (Corrected to apply to all matching gates) ---
                        // Only add this option if it's a valid gate, not an error marker.
                        if (!cell.getText().equals("❌")) {
                            contextMenu.getItems().add(new SeparatorMenuItem());
                            MenuItem setColorMenuItem = new MenuItem("Set Color...");

                            setColorMenuItem.setOnAction(e -> {
                                // Create a custom dialog to safely host the ColorPicker.
                                Dialog<Color> colorDialog = new Dialog<>();
                                colorDialog.setTitle("Choose Gate Color");
                                colorDialog.setHeaderText("Select a color for all '" + cell.getText() + "' gates.");

                                // Set the owner window to prevent the NullPointerException crash.
                                colorDialog.initOwner(cell.getScene().getWindow());

                                final ColorPicker dialogColorPicker = new ColorPicker();
                                colorDialog.getDialogPane().setContent(dialogColorPicker);
                                colorDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

                                // Convert the result to a Color object when the OK button is clicked.
                                colorDialog.setResultConverter(dialogButton -> {
                                    if (dialogButton == ButtonType.OK) {
                                        return dialogColorPicker.getValue();
                                    }
                                    return null;
                                });

                                // Show the dialog and wait for the user to choose a color.
                                colorDialog.showAndWait().ifPresent(newColor -> {
                                    String gateTypeToColor = cell.getText();
                                    String hexColor = String.format("#%02X%02X%02X",
                                            (int) (newColor.getRed() * 255),
                                            (int) (newColor.getGreen() * 255),
                                            (int) (newColor.getBlue() * 255));

                                    // Apply to all cells of the same time
                                    for (int i = 0; i < qubits; i++) {
                                        for (int j = 0; j < steps; j++) {
                                            if (cellButtons[i][j] != null && cellButtons[i][j].getText().equals(gateTypeToColor)) {
                                                cellButtons[i][j].setStyle("-fx-background-color: " + hexColor + ";");
                                            }
                                        }
                                    }
                                });
                            });
                            contextMenu.getItems().add(setColorMenuItem);
                        }

                        // Show the context menu at the mouse position.
                        contextMenu.show(cell, event.getScreenX(), event.getScreenY());
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
            if (playModeButtons != null) playModeButtons.setVisible(playMode);

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

    public String exportToJson() {
        StringBuilder json = new StringBuilder();
        json.append("{\"qubits\":").append(qubits).append(",\"gates\":[");
        boolean first = true;

        for (int step = 0; step < stepOperations.size(); step++) {
            for (GateOperation op : stepOperations.get(step)) {
                if (!first) json.append(",");
                json.append("{\"type\":\"").append(op.gateName.toUpperCase()).append("\",");
                json.append("\"step\":").append(step).append(",");
                json.append("\"targets\":[");
                for (int i = 0; i < op.qubits.length; i++) {
                    json.append(op.qubits[i]);
                    if (i < op.qubits.length - 1) json.append(",");
                }
                json.append("]}");
                first = false;
            }
        }

        json.append("]}");
        return json.toString();
    }

    public BottomPanel getBottomPanel() {
        return bottomPanel;
    }

    public void loadPatternFromJson(String json) {
        try {
            // Parse JSON data into temporary structures ---
            int qIdx = json.indexOf("\"qubits\":") + 9;
            int qEnd = json.indexOf(',', qIdx);
            int parsedQubits = Integer.parseInt(json.substring(qIdx, qEnd));

            // A temporary list to hold the parsed operations.
            List<List<GateOperation>> tempStepOperations = new ArrayList<>();
            int maxStep = -1;

            // Find the gates array within the JSON string.
            int gatesStart = json.indexOf("\"gates\":[") + 9;
            int gatesEnd = json.lastIndexOf("]}");
            String gatesJsonContent = json.substring(gatesStart, gatesEnd);

            if (!gatesJsonContent.trim().isEmpty()) {
                // Split into individual gate objects.
                String[] gateObjects = gatesJsonContent.replace("{", "").split("},");
                for (String g : gateObjects) {
                    String gateStr = g.replace("}", "").trim();
                    if (gateStr.isEmpty()) continue;

                    String[] parts = gateStr.split(",");
                    String type = "";
                    int step = 0;
                    int[] targets = new int[0];

                    for (String part : parts) {
                        String[] kv = part.split(":");
                        String key = kv[0].replaceAll("\"", "").trim();
                        String value = kv[1].trim();

                        switch (key) {
                            case "type":
                                type = value.replaceAll("\"", "");
                                break;
                            case "step":
                                step = Integer.parseInt(value);
                                if (step > maxStep) maxStep = step;
                                break;
                            case "targets":
                                String targetsValue = value.replace("[", "").replace("]", "");
                                if (!targetsValue.isEmpty()) {
                                    String[] targetsStr = targetsValue.split(",");
                                    targets = new int[targetsStr.length];
                                    for (int i = 0; i < targetsStr.length; i++) {
                                        targets[i] = Integer.parseInt(targetsStr[i].trim());
                                    }
                                }
                                break;
                        }
                    }

                    // Ensure our temporary list is large enough
                    while (tempStepOperations.size() <= step) {
                        tempStepOperations.add(new ArrayList<>());
                    }
                    tempStepOperations.get(step).add(new GateOperation(type.toLowerCase(), targets));
                }
            }

            // Update the panel's state and UI controls
            this.qubits = parsedQubits;
            this.steps = (maxStep == -1) ? 1 : maxStep + 1; // Handle empty circuits, ensure at least 1 step

            qubitsSpinner.getValueFactory().setValue(this.qubits);
            stepsSpinner.getValueFactory().setValue(this.steps);

            // Redraw the grid. This creates a new, empty `stepOperations` of the correct size.
            drawGrid();

            // Populate the new `stepOperations` and update the UI grid
            // The `stepOperations` field was just reset by drawGrid(). Now we fill it.
            for (int c = 0; c < tempStepOperations.size() && c < this.steps; c++) {
                for (GateOperation op : tempStepOperations.get(c)) {
                    // Add the operation to the official list
                    this.stepOperations.get(c).add(op);

                    // Update the button on the grid visually, checking bounds
                    if (op.qubits.length > 0 && op.qubits[0] < this.qubits && c < this.steps) {
                        Button cell = cellButtons[op.qubits[0]][c];
                        cell.setText(op.gateName.toUpperCase());
                        cell.getStyleClass().add("gate-" + op.gateName.toUpperCase());
                    }
                }
            }

            patternDisplayArea.setText("Circuit loaded from DB.");

        } catch (Exception e) {
            e.printStackTrace();
            patternDisplayArea.setText("Failed to parse or load circuit: " + e.getMessage());
            // On failure, reset to a known good state to prevent instability
            this.qubits = 3;
            this.steps = 5;
            qubitsSpinner.getValueFactory().setValue(this.qubits);
            stepsSpinner.getValueFactory().setValue(this.steps);
            drawGrid();
        }
    }

    private ServerResponse sendToServer(ClientRequest request) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            if (response instanceof ServerResponse serverResponse) {
                return serverResponse;
            } else {
                System.err.println("Invalid response from server.");
                return null;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with server: " + e.getMessage());
            return null;
        }
    }
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    // Build a short label like: "3q · 7 gates · 5 steps · [H q0, CX q0->q1, Z q2]"
    private String summarizeCircuitJson(String json) {
        try {
            int qubits = parseIntAfter(json, "\"qubits\":");
            int gatesCount = countOccurrences(json, "\"type\"");
            int maxStep = parseMaxIntAfter(json, "\"step\":");
            String preview = buildGatePreview(json, 3); // first 3 gates
            return String.format("%dq · %d gates · %d steps · %s",
                    qubits, gatesCount, Math.max(1, maxStep + 1), preview);
        } catch (Exception e) {
            return "(unknown) " + truncate(json, 60);
        }
    }

    private int parseIntAfter(String s, String key) {
        int i = s.indexOf(key);
        if (i < 0) return 0;
        i += key.length();
        while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        int j = i;
        while (j < s.length() && (Character.isDigit(s.charAt(j)))) j++;
        return Integer.parseInt(s.substring(i, j));
    }

    private int parseMaxIntAfter(String s, String key) {
        int max = -1, start = 0;
        while (true) {
            int i = s.indexOf(key, start);
            if (i < 0) break;
            i += key.length();
            while (i < s.length() && (s.charAt(i) == ' ' || s.charAt(i) == '\t')) i++;
            int j = i;
            while (j < s.length() && (Character.isDigit(s.charAt(j)))) j++;
            try { max = Math.max(max, Integer.parseInt(s.substring(i, j))); } catch (Exception ignored) {}
            start = j;
        }
        return max;
    }

    private int countOccurrences(String s, String needle) {
        int count = 0, from = 0;
        while (true) {
            int i = s.indexOf(needle, from);
            if (i < 0) break;
            count++;
            from = i + needle.length();
        }
        return count;
    }

    private String buildGatePreview(String json, int maxGates) {
        // very light parser: finds "type":"XYZ" and "targets":[a,b]
        java.util.List<String> parts = new java.util.ArrayList<>();
        int from = 0, taken = 0;
        while (taken < maxGates) {
            int t = json.indexOf("\"type\"", from);
            if (t < 0) break;
            int colon = json.indexOf(':', t);
            int q1 = json.indexOf('"', colon + 1);
            int q2 = json.indexOf('"', q1 + 1);
            String type = (q1 > 0 && q2 > q1) ? json.substring(q1 + 1, q2) : "?";

            int tar = json.indexOf("\"targets\"", q2);
            int lb = (tar < 0) ? -1 : json.indexOf('[', tar);
            int rb = (lb < 0) ? -1 : json.indexOf(']', lb);
            String targets = (lb > 0 && rb > lb) ? json.substring(lb + 1, rb).replaceAll("\\s+", "") : "";

            parts.add(type + " " + formatTargets(targets));
            taken++;
            from = (rb > 0 ? rb : q2 + 1);
        }
        return "[" + String.join(", ", parts) + "]";
    }

    private String formatTargets(String targetsCsv) {
        if (targetsCsv.isEmpty()) return "";
        String[] nums = targetsCsv.split(",");
        if (nums.length == 1) return "q" + nums[0];
        if (nums.length == 2) return "q" + nums[0] + "->q" + nums[1];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nums.length; i++) {
            if (i > 0) sb.append(',');
            sb.append("q").append(nums[i]);
        }
        return sb.toString();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }


}
