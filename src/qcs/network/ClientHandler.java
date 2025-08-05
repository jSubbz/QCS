package qcs.network;

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
            // Set up streams
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            System.out.println("Client handler started.");

            // Communication loop
            while (true) {
                Object obj = in.readObject();

                if (obj instanceof String message) {
                    System.out.println("Received from client: " + message);

                    // Echo it back for now
                    out.writeObject("Server received: " + message);
                    out.flush();

                    if (message.equalsIgnoreCase("exit")) {
                        break;
                    }
                }
            }

            socket.close();
            System.out.println("Client disconnected.");

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("ClientHandler error: " + e.getMessage());
        }
    }
}
