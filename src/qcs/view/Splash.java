package qcs.view;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
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
    private static final double SPLASH_DURATION_SECONDS = 3.0;

    public Splash() {
        // 1. Create the main image view
        ImageView splashImageView = loadSplashImage();

        // 2. Create the label for the team name
        Label teamLabel = new Label("Created by Team Quantum");
        // Style the label for better visibility (white text with a subtle drop shadow)
        teamLabel.setStyle(
                "-fx-font-size: 14px; " +
                        "-fx-text-fill: white; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.75), 3, 0, 0, 1);"
        );

        // 3. Use a StackPane to overlay the label on the image
        StackPane root = new StackPane(splashImageView, teamLabel);
        root.setStyle("-fx-background-color: transparent;"); // Allows for non-rectangular splash images

        // 4. Position the label at the bottom-center of the StackPane
        StackPane.setAlignment(teamLabel, Pos.BOTTOM_CENTER);
        // Add some padding so the label isn't flush with the bottom edge
        teamLabel.setPadding(new Insets(0, 0, 20, 0));

        // --- Scene and Stage Setup ---
        Scene scene = new Scene(root, 400, 250, Color.TRANSPARENT);
        splashStage = new Stage(StageStyle.TRANSPARENT); // An undecorated window
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
        String imagePath = "/QClogo.jpg";

        try {
            URL imageUrl = getClass().getResource(imagePath);

            // IMPORTANT: Check if the resource was found before using it to prevent a crash
            if (imageUrl == null) {
                System.err.println("Splash image not found!");
            } else {
                // If found, create the Image using the URL's external form
                Image splashImage = new Image(imageUrl.toExternalForm());
                imageView.setImage(splashImage);
                imageView.setFitWidth(400);
                imageView.setFitHeight(250);
                imageView.setPreserveRatio(true);
            }
        } catch (Exception e) {
            System.err.println("Failed to load splash image from path: " + imagePath);
        }
        return imageView;
    }
}
