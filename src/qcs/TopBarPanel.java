package qcs;

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import qcs.EventBus;
public class TopBarPanel {
    private HBox panel;

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

    public HBox getPanel() {
        return panel;
    }
}
