package qcs.network;

import qcs.db.DatabaseManager;

import java.io.*;
import java.net.Socket;
import java.util.List;

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

            DatabaseManager.createCircuitTable(); // Ensure table exists

            System.out.println("Client handler started.");

            while (true) {
                Object obj = in.readObject();

                if (obj instanceof ClientRequest request) {
                    switch (request.getType()) {
                        case LOGIN:
                            handleLogin(request);
                            break;
                        case SIGNUP:
                            handleSignup(request);
                            break;
                        case CIRCUIT_DATA:
                            handleCircuitData((CircuitDataRequest) request);
                            break;
                        case LOAD_CIRCUIT:
                            handleLoadCircuit(request);
                            break;
                        case EXIT:
                            System.out.println("Client requested disconnect.");
                            socket.close();
                            return;
                        default:
                            sendResponse(false, "Unsupported request type.");
                            break;
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

    private void handleCircuitData(CircuitDataRequest req) throws IOException {
        boolean saved = DatabaseManager.saveCircuit(req.getUsername(), req.getCircuitJson());
        if (saved) {
            sendResponse(true, "Circuit saved successfully.");
        } else {
            sendResponse(false, "Failed to save circuit.");
        }
    }

    private void handleLoadCircuit(ClientRequest request) throws IOException {
        List<String> circuits = DatabaseManager.getCircuitsForUser(request.getUsername());

        ServerResponse loadResponse = new ServerResponse();
        loadResponse.setSuccess(true);
        loadResponse.setMessage("Circuits loaded successfully.");
        loadResponse.setCircuits(circuits);

        out.writeObject(loadResponse);
        out.flush();
    }

    private void sendResponse(boolean success, String message) throws IOException {
        ServerResponse response = new ServerResponse();
        response.setSuccess(success);
        response.setMessage(message);
        out.writeObject(response);
        out.flush();
    }
}
