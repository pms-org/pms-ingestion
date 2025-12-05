package com.pms.ingestion.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public class QuickDatabaseTest {
    
    private static final String URL = "jdbc:postgresql://db-instance-pms.cvk4yqey0ex7.us-east-2.rds.amazonaws.com:5432/pms_db";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "PMS.2025";
    
    public static void main(String[] args) {
        System.out.println("🚀 Quick Database Write Test...");
        
        try {
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            
            // Insert a test record
            String insertSQL = "INSERT INTO safe_store_trade (id, received_at, portfolio_id, trade_id, symbol, side, price_per_stock, quantity, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = connection.prepareStatement(insertSQL);
            
            UUID testId = UUID.randomUUID();
            UUID portfolioId = UUID.randomUUID();
            UUID tradeId = UUID.randomUUID();
            
            pstmt.setObject(1, testId);
            pstmt.setObject(2, Instant.now());
            pstmt.setObject(3, portfolioId);
            pstmt.setObject(4, tradeId);
            pstmt.setString(5, "TEST");
            pstmt.setString(6, "BUY");
            pstmt.setDouble(7, 100.50);
            pstmt.setLong(8, 10L);
            pstmt.setObject(9, LocalDateTime.now());
            
            int rows = pstmt.executeUpdate();
            System.out.println("✅ Inserted " + rows + " test record");
            
            // Verify the insert
            ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM safe_store_trade");
            if (rs.next()) {
                System.out.println("📊 Total records now: " + rs.getInt(1));
            }
            
            connection.close();
            System.out.println("🎉 Test completed successfully!");
            
        } catch (Exception e) {
            System.out.println("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}