package qcs;
import javafx.scene.control.Button;
import qcs.EventBus;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.geometry.Insets;

public class LeftPanel implements EventBus.EventListener {
    private VBox panel;
    private Label gateLabel, modeLabel;

    public LeftPanel() {
        gateLabel = new Label();
        modeLabel = new Label();

        VBox gateButtons = new VBox(5); // Spacing between buttons
        gateButtons.setPadding(new Insets(5));

        var bundle = SettingsManager.getInstance().getBundle();

        // Internationalized single qubit gate buttons
        String[] singleQubitGates = {"I", "X", "Y", "Z", "H", "S", "T", "U"};
        for (String gate : singleQubitGates) {
            String label = bundle.containsKey("gate." + gate) ? bundle.getString("gate." + gate) : gate;
            Button btn = new Button(label);
            btn.setPrefWidth(50);
            btn.setOnAction(e -> System.out.println("Gate " + gate + " selected"));
            btn.getStyleClass().add("gate-button");
            gateButtons.getChildren().add(btn);
        }

        // Internationalized multi-qubit gate buttons
        String[] multiQubitGates = {"CX", "SWAP", "CU", "CCX"};
        for (String gate : multiQubitGates) {
            String label = bundle.containsKey("gate." + gate) ? bundle.getString("gate." + gate) : gate;
            Button btn = new Button(label);
            btn.setPrefWidth(50);
            btn.setOnAction(e -> System.out.println("Multi-qubit gate " + gate + " selected"));
            btn.getStyleClass().add("gate-button");
            gateButtons.getChildren().add(btn);
        }

        panel = new VBox(10, gateLabel, modeLabel, gateButtons);
        panel.setPadding(new Insets(10));
        EventBus.getInstance().subscribe(this);
        updateUI();
    }



    public void updateUI() {
        var bundle = SettingsManager.getInstance().getBundle();
        String mode = SettingsManager.getInstance().isDesignMode() ? "Design" : "Play";
        gateLabel.setText(bundle.getString("quantumGates"));
        modeLabel.setText(String.format(bundle.getString("currentMode"), mode));

        // Optional: Refresh button text when language changes
        panel.getChildren().filtered(node -> node instanceof VBox).forEach(container -> {
            ((VBox) container).getChildren().forEach(buttonNode -> {
                if (buttonNode instanceof Button) {
                    Button btn = (Button) buttonNode;
                    String gate = btn.getText(); // Original label
                    String newLabel = bundle.containsKey("gate." + gate) ? bundle.getString("gate." + gate) : gate;
                    btn.setText(newLabel);
                }
            });
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
