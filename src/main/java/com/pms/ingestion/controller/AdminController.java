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

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Admin API")
public class AdminController {

    private final SafeStoreRepository safeStoreRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final DlqRepository dlqRepository;

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
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
