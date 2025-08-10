package qcs;

import javafx.application.Application;
import qcs.view.LoginScreen;

import java.net.InetSocketAddress;
import java.net.Socket;

public class Launcher {

    private static final String HOST = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        Thread serverThread = new Thread(() -> {
            try {
                qcs.network.Server.main(new String[]{});
            } catch (Throwable t) {
                System.err.println("Server failed to start: " + t.getMessage());
                t.printStackTrace();
            }
        }, "QCS-Server");
        serverThread.setDaemon(true);
        serverThread.start();

        waitForServer(HOST, PORT, 6000);

        Application.launch(LoginScreen.class, args);
    }

    private static void waitForServer(String host, int port, int timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress(host, port), 500);
                System.out.println("✅ Server is up on " + host + ":" + port);
                return;
            } catch (Exception ignored) {
                try { Thread.sleep(150); } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
        System.err.println("⚠️  Server didn't confirm in time; login may fail until it finishes starting.");
    }
}
