import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// DATABASE CONNECTIVITY: Manages JDBC connections to MySQL database (bankdb)
public class DBConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/bankdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "Sindhu@12&23"; // Modify password to match local MySQL setup

    static {
        try {
            // Load MySQL JDBC Driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("CRITICAL ERROR: MySQL JDBC Driver not found on classpath!");
        }
    }

    // Returns a open database connection instance
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
