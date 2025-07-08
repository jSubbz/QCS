package qcs;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import qcs.EventBus;

import java.io.*;
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
    public void drawGrid() {
        gridPane.getChildren().clear();
        gridPane.getColumnConstraints().clear();
        gridPane.getRowConstraints().clear();

        cellButtons = new Button[qubits][steps];

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
                    if (selectedGate != null) {
                        cell.setText(selectedGate);
                        cell.getStyleClass().add("gate-" + selectedGate);

                        if (SettingsManager.getInstance().isDesignMode()) {
                            patternDisplayArea.appendText(String.format("%d,%d,%s\n", finalR, finalC, selectedGate));
                        }
                    } else {
                        cell.setText("");
                    }

                    if (bottomPanel != null)
                        bottomPanel.updateStatus((selectedGate != null ? selectedGate : "Cleared") + " at " + finalR + "," + finalC);
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
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(qubits + "," + steps + "\n");
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

    /**
     * Loads a grid pattern from a file.
     *
     * @param file File to load the pattern from.
     */
    public void loadPatternFromFile(File file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            patternDisplayArea.setText(content.toString());

            String[] lines = content.toString().split("\n");
            String[] dims = lines[0].split(",");
            qubits = Integer.parseInt(dims[0]);
            steps = Integer.parseInt(dims[1]);

            qubitsSpinner.getValueFactory().setValue(qubits);
            stepsSpinner.getValueFactory().setValue(steps);
            drawGrid();

            for (int i = 1; i < lines.length; i++) {
                String[] parts = lines[i].split(",");
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
