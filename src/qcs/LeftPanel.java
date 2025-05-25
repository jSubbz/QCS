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
import java.util.ArrayList;
import java.util.List;

public class LeftPanel implements EventBus.EventListener {
    private VBox panel;
    private Label gateLabel, modeLabel;
    private Label phaseLabel, phaseALabel, phaseBLabel, phaseCLabel;
    private Button barrierBtn;
    private List<Button> gateButtons = new ArrayList<>();  // 🔥 Store all gate buttons

    public LeftPanel() {
        gateLabel = new Label();
        modeLabel = new Label();

        var bundle = SettingsManager.getInstance().getBundle();

        Label singleQubitLabel = new Label("Single Qubit");
        singleQubitLabel.setStyle("-fx-font-weight: bold;");

        GridPane singleQubitGrid = new GridPane();
        singleQubitGrid.setHgap(5);
        singleQubitGrid.setVgap(5);
        String[] singleQubitGates = {"I", "X", "Y", "Z", "H", "S", "T", "U"};
        for (int i = 0; i < singleQubitGates.length; i++) {
            String gate = singleQubitGates[i];
            String label = bundle.containsKey("gate." + gate) ? bundle.getString("gate." + gate) : gate;
            Button btn = new Button(label);
            btn.setUserData(gate);
            btn.setPrefWidth(50);
            btn.setPrefHeight(30);
            btn.setOnAction(e -> EventBus.getInstance().publish("gateSelected:" + gate));
            btn.getStyleClass().add("gate-button");
            btn.getStyleClass().add("gate-button-" + gate);
            singleQubitGrid.add(btn, i % 2, i / 2);
            gateButtons.add(btn);  // 🔥 Add to list
        }

        Label multiQubitLabel = new Label("Multi Qubit");
        multiQubitLabel.setStyle("-fx-font-weight: bold;");

        VBox multiQubitBox = new VBox(5);
        String[] multiQubitGates = {"CX", "SWAP", "CU", "CCX"};
        for (String gate : multiQubitGates) {
            String label = bundle.containsKey("gate." + gate) ? bundle.getString("gate." + gate) : gate;
            Button btn = new Button(label);
            btn.setUserData(gate);
            btn.setPrefWidth(100);
            btn.setPrefHeight(30);
            btn.setOnAction(e -> EventBus.getInstance().publish("gateSelected:" + gate));
            btn.getStyleClass().add("gate-button");
            btn.getStyleClass().add("gate-button-" + gate);
            multiQubitBox.getChildren().add(btn);
            gateButtons.add(btn);  // 🔥 Add to list
        }

        Label operationsLabel = new Label("Operations");
        operationsLabel.setStyle("-fx-font-weight: bold;");

        barrierBtn = new Button(bundle.getString("gate.BARRIER"));
        barrierBtn.setPrefWidth(100);
        barrierBtn.setUserData("BARRIER");
        barrierBtn.setOnAction(e -> EventBus.getInstance().publish("gateSelected:BARRIER"));
        barrierBtn.getStyleClass().add("gate-button");
        barrierBtn.getStyleClass().add("gate-button-BARRIER");
        HBox barrierBox = new HBox(barrierBtn);
        barrierBox.setPadding(new Insets(5));
        barrierBox.setAlignment(Pos.CENTER);

        phaseLabel = new Label(bundle.getString("phaseParams"));
        phaseALabel = new Label(bundle.getString("phaseA"));
        phaseBLabel = new Label(bundle.getString("phaseB"));
        phaseCLabel = new Label(bundle.getString("phaseC"));

        TextField aField = new TextField("0.0");
        aField.setPrefWidth(50);
        TextField bField = new TextField("0.0");
        bField.setPrefWidth(50);
        TextField cField = new TextField("0.0");
        cField.setPrefWidth(50);

        VBox phaseBox = new VBox(2, phaseLabel,
                new HBox(2, phaseALabel, aField),
                new HBox(2, phaseBLabel, bField),
                new HBox(2, phaseCLabel, cField)
        );

        panel = new VBox(10, gateLabel, modeLabel,
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
        modeLabel.setText(bundle.getString(modeKey));

        // 🔥 Update all gate buttons, including SWAP
        for (Button btn : gateButtons) {
            String gate = (String) btn.getUserData();
            String newLabel = bundle.containsKey("gate." + gate) ? bundle.getString("gate." + gate) : gate;
            btn.setText(newLabel);
        }

        barrierBtn.setText(bundle.getString("gate.BARRIER"));
        phaseLabel.setText(bundle.getString("phaseParams"));
        phaseALabel.setText(bundle.getString("phaseA"));
        phaseBLabel.setText(bundle.getString("phaseB"));
        phaseCLabel.setText(bundle.getString("phaseC"));
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
