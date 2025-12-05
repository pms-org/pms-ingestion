package com.pms.ingestion.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseDataChecker {
    
    private static final String URL = "jdbc:postgresql://db-instance-pms.cvk4yqey0ex7.us-east-2.rds.amazonaws.com:5432/pms_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "PMS.2025";
    
    public static void main(String[] args) {
        System.out.println("Checking AWS RDS database for data...");
        
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            Statement stmt = connection.createStatement();
            
            // Check safe_store_trade table
            System.out.println("\n=== SAFE_STORE_TRADE TABLE ===");
            ResultSet rs1 = stmt.executeQuery("SELECT COUNT(*) as count FROM safe_store_trade");
            if (rs1.next()) {
                int count = rs1.getInt("count");
                System.out.println("Total records: " + count);
                
                if (count > 0) {
                    ResultSet rs2 = stmt.executeQuery("SELECT * FROM safe_store_trade ORDER BY received_at DESC LIMIT 5");
                    System.out.println("\nLatest 5 records:");
                    while (rs2.next()) {
                        System.out.println("ID: " + rs2.getString("id") + 
                                         ", Trade ID: " + rs2.getString("trade_id") + 
                                         ", Symbol: " + rs2.getString("symbol") + 
                                         ", Received: " + rs2.getTimestamp("received_at"));
                    }
                }
            }
            
            // Check outbox_trade table
            System.out.println("\n=== OUTBOX_TRADE TABLE ===");
            ResultSet rs3 = stmt.executeQuery("SELECT COUNT(*) as count FROM outbox_trade");
            if (rs3.next()) {
                int count = rs3.getInt("count");
                System.out.println("Total records: " + count);
                
                if (count > 0) {
                    ResultSet rs4 = stmt.executeQuery("SELECT * FROM outbox_trade ORDER BY created_at DESC LIMIT 5");
                    System.out.println("\nLatest 5 records:");
                    while (rs4.next()) {
                        System.out.println("ID: " + rs4.getString("id") + 
                                         ", Trade ID: " + rs4.getString("trade_id") + 
                                         ", Symbol: " + rs4.getString("symbol") + 
                                         ", Status: " + rs4.getString("status") + 
                                         ", Created: " + rs4.getTimestamp("created_at"));
                    }
                }
            }
            
            // Check dlq_trade table
            System.out.println("\n=== DLQ_TRADE TABLE ===");
            ResultSet rs5 = stmt.executeQuery("SELECT COUNT(*) as count FROM dlq_trade");
            if (rs5.next()) {
                int count = rs5.getInt("count");
                System.out.println("Total records: " + count);
                
                if (count > 0) {
                    ResultSet rs6 = stmt.executeQuery("SELECT * FROM dlq_trade ORDER BY failed_at DESC LIMIT 3");
                    System.out.println("\nLatest 3 error records:");
                    while (rs6.next()) {
                        System.out.println("ID: " + rs6.getString("id") + 
                                         ", Error: " + rs6.getString("error_detail") + 
                                         ", Failed: " + rs6.getTimestamp("failed_at"));
                    }
                }
            }
            
            connection.close();
            System.out.println("\n✅ Database check completed successfully!");
            
        } catch (Exception e) {
            System.out.println("❌ Error checking database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}