import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // Database credentials
    private static final String URL = "jdbc:mysql://localhost:3306/student_enrollment";
    private static final String USER = "root"; // Default XAMPP/MySQL username
    private static final String PASSWORD = "password"; // Default XAMPP/MySQL password (often empty)

    public static Connection getConnection() {
        Connection connection = null;
        try {
            // Establish the connection
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Database connection successful!");
        } catch (SQLException e) {
            System.out.println("Database connection failed!");
            e.printStackTrace();
        }
        return connection;
    }

    // Quick test to see if it works
    public static void main(String[] args) {
        getConnection();
    }
}