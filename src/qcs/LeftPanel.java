package qcs;
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
        panel = new VBox(10, gateLabel, modeLabel);
        panel.setPadding(new Insets(10));
        updateUI();
        EventBus.getInstance().subscribe(this);
    }

    public void updateUI() {
        var bundle = SettingsManager.getInstance().getBundle();
        String mode = SettingsManager.getInstance().isDesignMode() ? "Design" : "Play";
        gateLabel.setText(bundle.getString("quantumGates"));
        modeLabel.setText(String.format(bundle.getString("currentMode"), mode));
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
