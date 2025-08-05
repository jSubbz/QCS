package qcs.db;

import java.sql.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:qcs_users.db"; // Local file
    private static final String USERS_TABLE = """
        CREATE TABLE IF NOT EXISTS Users (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            username TEXT UNIQUE NOT NULL,
            password_hash TEXT NOT NULL
        );
    """;

    private static Connection conn;

    // Call this once to initialize the DB
    public static void initializeDatabase() {
        try {
            conn = DriverManager.getConnection(DB_URL);
            Statement stmt = conn.createStatement();
            stmt.execute(USERS_TABLE);
            stmt.close();
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize DB: " + e.getMessage());
        }
    }

    public static boolean createUser(String username, String password) {
        String sql = "INSERT INTO Users (username, password_hash) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, hashPassword(password));
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Signup error: " + e.getMessage());
            return false;
        }
    }

    public static boolean validateUser(String username, String password) {
        String sql = "SELECT password_hash FROM Users WHERE username = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return storedHash.equals(hashPassword(password));
            }
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
        }
        return false;
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Password hashing failed.");
            return password; // fallback (not secure)
        }
    }

    public static void closeConnection() {
        try {
            if (conn != null) conn.close();
        } catch (SQLException e) {
            System.err.println("Error closing DB: " + e.getMessage());
        }
    }
}
