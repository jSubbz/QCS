package qcs.network;

import java.io.*;
import java.net.Socket;

public class Client {

    public static final String SERVER_HOST = "localhost";
    public static final int SERVER_PORT = 12345;


    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connectToServer() {
        try {
            socket = new Socket(SERVER_HOST, SERVER_PORT);
            System.out.println("Connected to server.");

            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            // Test message to server
            sendMessage("Hello Server!");

            // Read reply
            Object response = in.readObject();
            if (response instanceof String reply) {
                System.out.println("Server says: " + reply);
            }

            // Clean exit
            sendMessage("exit");
            socket.close();

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Client error: " + e.getMessage());
        }
    }

    public void sendMessage(String message) throws IOException {
        out.writeObject(message);
        out.flush();
    }
}
