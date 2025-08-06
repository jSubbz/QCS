package qcs.network;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;

/**
 * A singleton network client that manages a persistent connection to the server.
 * This avoids creating a new socket for every request.
 */
public class NetworkClient {

    private static NetworkClient instance;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    // Private constructor to enforce singleton pattern
    private NetworkClient() {}

    /**
     * Gets the singleton instance of the NetworkClient.
     */
    public static synchronized NetworkClient getInstance() {
        if (instance == null) {
            instance = new NetworkClient();
        }
        return instance;
    }

    /**
     * Establishes a connection to the server.
     * @return true if connection is successful, false otherwise.
     */
    public synchronized boolean connect() {
        try {
            if (socket == null || socket.isClosed()) {
                socket = new Socket(SERVER_HOST, SERVER_PORT);
                // IMPORTANT: The order of stream creation matters to avoid deadlocks.
                // Create output stream first, then input stream.
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());
                System.out.println("✅ Successfully connected to the server.");
                return true;
            }
        } catch (IOException e) {
            System.err.println("❌ Connection failed: " + e.getMessage());
            return false;
        }
        return true; // Already connected
    }

    /**
     * Sends a request to the server over the persistent connection.
     * This method is synchronized to prevent multiple threads from writing at the same time.
     * @param request The request object to send.
     * @return The server's response, or null on failure.
     */
    public synchronized ServerResponse sendRequest(ClientRequest request) {
        if (socket == null || socket.isClosed() || out == null || in == null) {
            System.err.println("❌ Cannot send request: Not connected to server.");
            return null;
        }

        try {
            out.writeObject(request);
            out.flush();

            Object response = in.readObject();
            if (response instanceof ServerResponse serverResponse) {
                return serverResponse;
            }
        } catch (SocketException | EOFException e) {
            System.err.println("❌ Connection lost: " + e.getMessage());
            disconnect(); // Clean up on lost connection
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("❌ Error during communication: " + e.getMessage());
        }
        return null;
    }

    /**
     * Closes the connection to the server.
     */
    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                // Optionally send a clean EXIT message to the server
                sendRequest(new ClientRequest(RequestType.EXIT));
                socket.close();
                System.out.println("🔌 Disconnected from server.");
            }
        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        } finally {
            // Nullify resources
            socket = null;
            out = null;
            in = null;
        }
    }
}