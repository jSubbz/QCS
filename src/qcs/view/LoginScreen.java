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
import qcs.network.RequestType;
import qcs.network.ServerResponse;

import java.io.*;
import java.net.Socket;

public class LoginScreen extends Application {

    private TextField usernameField;
    private PasswordField passwordField;
    private Button signInButton, signUpButton;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

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

        ClientRequest request = new ClientRequest(RequestType.LOGIN);
        request.setUsername(user);
        request.setPassword(pass);

        ServerResponse response = sendToServer(request);

        if (response == null) {
            showAlert("Error", "Could not connect to server.");
        } else if (response.isSuccess()) {
            showAlert("Success", response.getMessage());

            try {
                new MainDriver().start(new Stage());
                stage.close();
            } catch (Exception e) {
                showAlert("Error", "Failed to launch client: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Login Failed", response.getMessage());
        }
    }

    private void handleSignUp() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText().trim();

        if (user.isEmpty() || pass.isEmpty()) {
            showAlert("Error", "Username and Password required.");
            return;
        }

        ClientRequest request = new ClientRequest(RequestType.SIGNUP);
        request.setUsername(user);
        request.setPassword(pass);

        ServerResponse response = sendToServer(request);

        if (response == null) {
            showAlert("Error", "Could not connect to server.");
        } else if (response.isSuccess()) {
            showAlert("Success", response.getMessage());
        } else {
            showAlert("Sign Up Failed", response.getMessage());
        }
    }

    private ServerResponse sendToServer(ClientRequest request) {
        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            if (response instanceof ServerResponse serverResponse) {
                return serverResponse;
            } else {
                System.err.println("Invalid response from server.");
                return null;
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with server: " + e.getMessage());
            return null;
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
