package qcs.network;

import qcs.db.DatabaseManager;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Client handler started.");

            while (true) {
                Object obj = in.readObject();

                if (obj instanceof ClientRequest request) {
                    switch (request.getType()) {
                        case LOGIN -> handleLogin(request);
                        case SIGNUP -> handleSignup(request);
                        case EXIT -> {
                            System.out.println("Client requested disconnect.");
                            socket.close();
                            return;
                        }
                        default -> sendResponse(false, "Unsupported request type.");
                    }
                } else {
                    sendResponse(false, "Invalid object received.");
                }
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("ClientHandler error: " + e.getMessage());
        }
    }

    private void handleLogin(ClientRequest req) throws IOException {
        String user = req.getUsername();
        String pass = req.getPassword();

        boolean valid = DatabaseManager.validateUser(user, pass);
        if (valid) {
            sendResponse(true, "Login successful.");
        } else {
            sendResponse(false, "Invalid credentials.");
        }
    }

    private void handleSignup(ClientRequest req) throws IOException {
        String user = req.getUsername();
        String pass = req.getPassword();

        boolean created = DatabaseManager.createUser(user, pass);
        if (created) {
            sendResponse(true, "Account created successfully.");
        } else {
            sendResponse(false, "Username already exists.");
        }
    }

    private void sendResponse(boolean success, String message) throws IOException {
        ServerResponse response = new ServerResponse(success, message);
        out.writeObject(response);
        out.flush();
    }
}
