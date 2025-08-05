package qcs.db;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:qcs.db";

    private static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // ✅ Initialize users and circuits tables
    public static void initializeDatabase() {
        try (Connection conn = connect()) {
            if (conn != null) {
                Statement stmt = conn.createStatement();

                // Users table
                String userTableSQL = """
                    CREATE TABLE IF NOT EXISTS users (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT UNIQUE NOT NULL,
                        password TEXT NOT NULL
                    );
                """;

                // Circuits table
                String circuitTableSQL = """
                    CREATE TABLE IF NOT EXISTS circuits (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL,
                        circuit_json TEXT NOT NULL,
                        timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
                    );
                """;

                stmt.execute(userTableSQL);
                stmt.execute(circuitTableSQL);

                System.out.println("✅ Database initialized.");
            }
        } catch (SQLException e) {
            System.err.println("❌ Failed to initialize database: " + e.getMessage());
        }
    }

    // ✅ Create a new user
    public static boolean createUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Signup error: " + e.getMessage());
            return false;
        }
    }

    // ✅ Validate login
    public static boolean validateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();  // user exists
        } catch (SQLException e) {
            System.err.println("Login error: " + e.getMessage());
            return false;
        }
    }

    // ✅ Save circuit to DB
    public static boolean saveCircuit(String username, String circuitJson) {
        String sql = "INSERT INTO circuits (username, circuit_json) VALUES (?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, circuitJson);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error saving circuit: " + e.getMessage());
            return false;
        }
    }



    // 🔹 Optional: can be used elsewhere if needed
    public static void createCircuitTable() {
        String sql = """
            CREATE TABLE IF NOT EXISTS circuits (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                circuit_json TEXT NOT NULL,
                timestamp DATETIME DEFAULT CURRENT_TIMESTAMP
            );
            """;

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Failed to create circuits table: " + e.getMessage());
        }
    }
    public static List<String> getCircuitsForUser(String username) {
        List<String> circuits = new ArrayList<>();
        String sql = "SELECT circuit_json FROM circuits WHERE username = ? ORDER BY timestamp DESC";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                circuits.add(rs.getString("circuit_json"));
            }
        } catch (SQLException e) {
            System.err.println("Error loading circuits: " + e.getMessage());
        }

        return circuits;
    }

}
