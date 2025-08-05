package qcs.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import qcs.db.DatabaseManager; // Import DB handler

public class LoginScreen extends Application {

    private TextField usernameField;
    private PasswordField passwordField;
    private Button signInButton, signUpButton;

    @Override
    public void start(Stage primaryStage) {
        // Initialize the database
        DatabaseManager.initializeDatabase();

        primaryStage.setTitle("Login - Quantum Circuit Simulator");

        Label titleLabel = new Label("QUANTUM CIRCUIT SIMULATOR");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        usernameField = new TextField();
        passwordField = new PasswordField();

        usernameField.setPromptText("Enter Username");
        passwordField.setPromptText("Enter Password");

        signInButton = new Button("Sign In");
        signUpButton = new Button("Sign Up");

        // Button Actions
        signInButton.setOnAction(e -> handleSignIn(primaryStage));
        signUpButton.setOnAction(e -> handleSignUp());

        VBox form = new VBox(10,
                titleLabel,
                new Label("Username:"), usernameField,
                new Label("Password:"), passwordField,
                signInButton,
                signUpButton
        );
        form.setPadding(new Insets(20));
        form.setAlignment(Pos.CENTER);

        Scene scene = new Scene(form, 400, 350);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void handleSignIn(Stage stage) {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Error", "Username and Password required.");
            return;
        }

        boolean valid = DatabaseManager.validateUser(user, pass);

        if (valid) {
            showAlert("Success", "Login successful!");

            // Launch the client GUI (replace with your actual class)
            try {
                new qcs.controller.MainDriver().start(new Stage());

                stage.close(); // Close login
            } catch (Exception e) {
                showAlert("Error", "Failed to launch client: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Error", "Invalid credentials.");
        }
    }

    private void handleSignUp() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Error", "Username and Password required.");
            return;
        }

        boolean created = DatabaseManager.createUser(user, pass);

        if (created) {
            showAlert("Success", "Account created. You can now sign in.");
        } else {
            showAlert("Error", "Username already exists or error occurred.");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
