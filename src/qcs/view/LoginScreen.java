package qcs.view;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import qcs.controller.MainDriver;
import qcs.network.ClientRequest;
import qcs.network.NetworkClient;
import qcs.network.RequestType;
import qcs.network.ServerResponse;
import qcs.util.SettingsManager;


public class LoginScreen extends Application {

    private TextField usernameField;
    private PasswordField passwordField;
    private Button signInButton, signUpButton;


    @Override
    public void start(Stage primaryStage) {
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

        // --- REFACTORED PART ---
        // 1. Get the singleton instance and connect. This connection will now persist.
        NetworkClient client = NetworkClient.getInstance();
        if (!client.connect()) {
            showAlert("Error", "Could not connect to server.");
            return;
        }

        // 2. Create and send the request using the persistent connection.
        ClientRequest request = new ClientRequest(RequestType.LOGIN);
        request.setUsername(user);
        request.setPassword(pass);
        ServerResponse response = client.sendRequest(request);
        // --- END REFACTORED PART ---

        if (response != null && response.isSuccess()) {
            showAlert("Success", response.getMessage());

            SettingsManager.getInstance().setUsername(user);

            try {
                new MainDriver().start(new Stage());
                stage.close();
            } catch (Exception e) {
                showAlert("Error", "Failed to launch client: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Login Failed", response != null ? response.getMessage() : "No response from server.");
        }
    }


    private void handleSignUp() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Error", "Username and Password required.");
            return;
        }

        // Use the singleton here as well
        NetworkClient client = NetworkClient.getInstance();
        if (!client.connect()) {
            showAlert("Error", "Could not connect to server.");
            return;
        }

        ClientRequest request = new ClientRequest(RequestType.SIGNUP);
        request.setUsername(user);
        request.setPassword(pass);

        ServerResponse response = client.sendRequest(request);

        if (response != null && response.isSuccess()) {
            showAlert("Success", response.getMessage());
        } else {
            showAlert("Sign Up Failed", response != null ? response.getMessage() : "No response from server.");
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