package qcs.view;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.net.URL;

/**
 * A splash screen that appears for a fixed duration before launching the main application window.
 * It displays an image and a team name label.
 */
public class Splash {

    private final Stage splashStage;
    private static final double SPLASH_DURATION_SECONDS = 2.0;

    /**
     * Constructs a new Splash object for the splash screen.
     */
    public Splash() {
        // Create the main image view
        ImageView splashImageView = loadSplashImage();

        // Create the label for the team name
        Label teamLabel = new Label("[Team: Jay Perry / Max Strange]");
        // Style the label for better visibility (dark text on light background)
        teamLabel.setStyle( "-fx-font-size: 14px; " +
                            "-fx-text-fill: #808080; ");

        //Create loading bar animation component
        ProgressBar loadingBar = new ProgressBar();
        loadingBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS); // This creates the animation
        loadingBar.setPrefWidth(400);

        // Group team name label and loading bar components
        VBox bottomComponents = new VBox( teamLabel, loadingBar);
        bottomComponents.setAlignment(Pos.CENTER);

        // Stack the components vertically in another VBox
        VBox root = new VBox( splashImageView, bottomComponents);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: #F4F4F4;");

        // Add some padding so the label isn't flush with the bottom edge


        // --- Scene and Stage Setup ---
        Scene scene = new Scene(root, 400, 245);
        splashStage = new Stage(StageStyle.UNDECORATED); // An undecorated window
        splashStage.setScene(scene);
        splashStage.setTitle("QCS Loading...");
    }

    /**
     * Shows the splash screen, waits for a fixed duration, then closes it
     * and shows the provided main application stage.
     *
     * @param mainStage The primary stage of the main application, which will be shown after the splash.
     */
    public void showAndSwitch(Stage mainStage) {
        splashStage.show();

        // Create a pause transition for the splash screen duration
        PauseTransition delay = new PauseTransition(Duration.seconds(SPLASH_DURATION_SECONDS));

        // Define what happens after the delay is finished
        delay.setOnFinished(e -> {
            splashStage.close(); // Close the splash screen
            mainStage.show();    // Show the main application window
        });

        // Start the delay
        delay.play();
    }

    /**
     * Creates and configures the ImageView for the splash screen.
     *
     * @return A configured ImageView.
     */
    private ImageView loadSplashImage() {
        ImageView imageView = new ImageView();
        String imagePath = "QClogo.jpg";

        try {
            URL imageUrl = getClass().getResource(imagePath);
            Image splashImage = new Image(imageUrl.toExternalForm());
            imageView.setImage(splashImage);
            imageView.setFitWidth(400);
            imageView.setFitHeight(200);
            imageView.setPreserveRatio(true);
        } catch (Exception e) {
            System.out.println("Failed to load splash image from path: " + imagePath);
        }
        return imageView;
    }
}
