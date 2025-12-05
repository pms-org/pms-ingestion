package com.pms.ingestion.controller;


import com.pms.ingestion.repository.DlqRepository;
import com.pms.ingestion.repository.OutboxEventRepository;
import com.pms.ingestion.repository.SafeStoreRepository;
import com.pms.ingestion.entity.DlqTrade;
import com.pms.ingestion.entity.OutboxTrade;
import com.pms.ingestion.entity.SafeStoreTrade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin API")
public class AdminController {

    private final SafeStoreRepository safeStoreRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final DlqRepository dlqRepository;
    private final DataSource dataSource;

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/db-connection")
    @Operation(summary = "Test AWS RDS database connection")
    public ResponseEntity<Map<String, Object>> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5); // 5 second timeout
            
            result.put("connected", isValid);
            result.put("database", connection.getCatalog());
            result.put("url", connection.getMetaData().getURL());
            result.put("driver", connection.getMetaData().getDriverName());
            result.put("driverVersion", connection.getMetaData().getDriverVersion());
            result.put("status", isValid ? "SUCCESS" : "FAILED");
            
            return ResponseEntity.ok(result);
            
        } catch (SQLException e) {
            result.put("connected", false);
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            return ResponseEntity.status(500).body(result);
        }
    }

    @GetMapping("/safe-store")
    @Operation(summary = "Get all safe store data")
    public ResponseEntity<List<SafeStoreTrade>> getAllSafeStoreData() {
        List<SafeStoreTrade> safeStoreData = safeStoreRepository.findAll();
        return ResponseEntity.ok(safeStoreData);
    }

    @GetMapping("/outbox")
    @Operation(summary = "Get all outbox events")
    public ResponseEntity<List<OutboxTrade>> getAllOutboxEvents() {
        List<OutboxTrade> outboxTrades = outboxEventRepository.findAll();
        return ResponseEntity.ok(outboxTrades);
    }

    @GetMapping("/dlq")
    @Operation(summary = "Get all DLQ events")
    public ResponseEntity<List<DlqTrade>> getAllDlqEvents() {
        List<DlqTrade> dlqEvents = dlqRepository.findAll();
        return ResponseEntity.ok(dlqEvents);
    }

}
