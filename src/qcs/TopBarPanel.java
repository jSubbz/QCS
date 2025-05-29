package qcs;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import qcs.EventBus;

/**
 * TopBarPanel creates a simple top bar with window control buttons: minimize, maximize, and close.
 * It currently includes placeholder actions for minimize and maximize, and an actual close action.
 */
public class TopBarPanel {
    private HBox panel;

    /**
     * Constructs the TopBarPanel and initializes the buttons.
     * The buttons are aligned to the top-right corner.
     */
    public TopBarPanel() {
        Button minimize = new Button("-");
        minimize.setOnAction(e -> System.out.println("Minimize pressed")); // Placeholder

        Button maximize = new Button("[]");
        maximize.setOnAction(e -> System.out.println("Maximize pressed")); // Placeholder

        Button close = new Button("X");
        close.setOnAction(e -> System.exit(0));

        panel = new HBox(10, minimize, maximize, close);
        panel.setAlignment(Pos.TOP_RIGHT);
    }

    /**
     * Returns the HBox containing the top bar buttons.
     *
     * @return HBox with window control buttons.
     */
    public HBox getPanel() {
        return panel;
    }
}
