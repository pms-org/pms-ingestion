package com.pms.ingestion.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnectionTest {
    
    private static final String URL = "jdbc:postgresql://db-instance-pms.cvk4yqey0ex7.us-east-2.rds.amazonaws.com:5432/pms_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "PMS.2025";
    
    public static void main(String[] args) {
        System.out.println("Testing AWS RDS PostgreSQL connection...");
        System.out.println("URL: " + URL);
        System.out.println("Username: " + USERNAME);
        
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            
            // Attempt connection
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            
            if (connection != null && !connection.isClosed()) {
                System.out.println("✅ SUCCESS: Connected to AWS RDS PostgreSQL database!");
                System.out.println("Database: " + connection.getCatalog());
                System.out.println("Connection valid: " + connection.isValid(5));
                
                // Close connection
                connection.close();
                System.out.println("Connection closed successfully.");
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("❌ ERROR: PostgreSQL driver not found!");
            System.out.println("Details: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println("❌ ERROR: Failed to connect to AWS RDS database!");
            System.out.println("SQL State: " + e.getSQLState());
            System.out.println("Error Code: " + e.getErrorCode());
            System.out.println("Details: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ ERROR: Unexpected error occurred!");
            System.out.println("Details: " + e.getMessage());
        }
    }
}