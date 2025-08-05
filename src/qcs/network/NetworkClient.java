package qcs.network;

import java.io.*;
import java.net.Socket;

public class NetworkClient {

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 12345;

    public static ServerResponse sendRequest(ClientRequest request) {
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
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error communicating with server: " + e.getMessage());
        }
        return null;
    }
}
