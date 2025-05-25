package qcs;

import javafx.scene.control.Button;
import qcs.EventBus;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.TextField;
import javafx.geometry.Pos;

public class LeftPanel implements EventBus.EventListener {
    private VBox panel;
    private Label gateLabel, modeLabel;

    public LeftPanel() {
        gateLabel = new Label();
        modeLabel = new Label();

        var bundle = SettingsManager.getInstance().getBundle();

        // 🔸 ADD Section Label for Single Qubit
        Label singleQubitLabel = new Label("Single Qubit");
        singleQubitLabel.setStyle("-fx-font-weight: bold;");

        GridPane singleQubitGrid = new GridPane();
        singleQubitGrid.setHgap(5);
        singleQubitGrid.setVgap(5);
        String[] singleQubitGates = {"I", "X", "Y", "Z", "H", "S", "T", "U"};
        for (int i = 0; i < singleQubitGates.length; i++) {
            String gate = singleQubitGates[i];
            String label = gate;  // Short label (I, X, H, etc.)
            Button btn = new Button(label);
            btn.setUserData(gate);
            btn.setPrefWidth(50);
            btn.setPrefHeight(30);
            btn.setOnAction(e -> EventBus.getInstance().publish("gateSelected:" + gate));
            btn.getStyleClass().add("gate-button");
            btn.getStyleClass().add("gate-button-" + gate);  // Optional: color styling
            singleQubitGrid.add(btn, i % 2, i / 2);
        }

        // 🔸 ADD Section Label for Multi Qubit
        Label multiQubitLabel = new Label("Multi Qubit");
        multiQubitLabel.setStyle("-fx-font-weight: bold;");

        VBox multiQubitBox = new VBox(5);
        String[] multiQubitGates = {"CX", "SWAP", "CU", "CCX"};
        for (String gate : multiQubitGates) {
            String label = gate;
            Button btn = new Button(label);
            btn.setUserData(gate);
            btn.setPrefWidth(100);
            btn.setPrefHeight(30);
            btn.setOnAction(e -> EventBus.getInstance().publish("gateSelected:" + gate));
            btn.getStyleClass().add("gate-button");
            btn.getStyleClass().add("gate-button-" + gate);  // Optional: color styling
            multiQubitBox.getChildren().add(btn);
        }

        // 🔸 ADD Section Label for Operations
        Label operationsLabel = new Label("Operations");
        operationsLabel.setStyle("-fx-font-weight: bold;");

        Button barrierBtn = new Button("BARRIER");
        barrierBtn.setPrefWidth(100);
        barrierBtn.setUserData("BARRIER");
        barrierBtn.setOnAction(e -> EventBus.getInstance().publish("gateSelected:BARRIER"));
        barrierBtn.getStyleClass().add("gate-button");
        barrierBtn.getStyleClass().add("gate-button-BARRIER");  // Optional: color styling
        HBox barrierBox = new HBox(barrierBtn);
        barrierBox.setPadding(new Insets(5));
        barrierBox.setAlignment(Pos.CENTER);

        // Phase Parameters
        TextField aField = new TextField("0.0");
        aField.setPrefWidth(50);
        TextField bField = new TextField("0.0");
        bField.setPrefWidth(50);
        TextField cField = new TextField("0.0");
        cField.setPrefWidth(50);

        VBox phaseBox = new VBox(2, new Label("Phase Params:"),
                new HBox(2, new Label("A:"), aField),
                new HBox(2, new Label("B:"), bField),
                new HBox(2, new Label("C:"), cField)
        );

        // 🔸 Assemble panel with section labels
        panel = new VBox(10,
                gateLabel,
                modeLabel,
                singleQubitLabel, singleQubitGrid,
                multiQubitLabel, multiQubitBox,
                operationsLabel, barrierBox,
                phaseBox);
        panel.setPadding(new Insets(10));

        EventBus.getInstance().subscribe(this);
        updateUI();
    }

    public void updateUI() {
        var bundle = SettingsManager.getInstance().getBundle();
        gateLabel.setText(bundle.getString("quantumGates"));

        String modeKey = SettingsManager.getInstance().isDesignMode() ? "designMode" : "playMode";
        String localizedMode = bundle.getString(modeKey);
        String modeDisplay = bundle.containsKey("modeDisplay") ?
                String.format(bundle.getString("modeDisplay"), localizedMode) : localizedMode;
        modeLabel.setText(modeDisplay);

        panel.getChildren().filtered(node -> node instanceof GridPane || node instanceof VBox).forEach(container -> {
            if (container instanceof GridPane grid) {
                grid.getChildren().forEach(node -> {
                    if (node instanceof Button btn) {
                        String gate = (String) btn.getUserData();
                        String newLabel = bundle.containsKey("gate." + gate) ? bundle.getString("gate." + gate) : gate;
                        btn.setText(newLabel);
                        btn.setPrefWidth(50);
                        btn.setPrefHeight(30);
                    }
                });
            } else if (container instanceof VBox box) {
                box.getChildren().forEach(node -> {
                    if (node instanceof Button btn) {
                        String gate = (String) btn.getUserData();
                        String newLabel = bundle.containsKey("gate." + gate) ? bundle.getString("gate." + gate) : gate;
                        btn.setText(newLabel);
                        btn.setPrefWidth(100);
                        btn.setPrefHeight(30);
                    }
                });
            }
        });
    }

    public VBox getPanel() {
        return panel;
    }

    @Override
    public void onEvent(String eventType) {
        if ("languageChanged".equals(eventType) || "modeToggled".equals(eventType)) {
            updateUI();
        }
    }
}
