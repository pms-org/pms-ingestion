package com.pms.ingestion.Controller;


import com.pms.ingestion.Dao.DlqDao;
import com.pms.ingestion.Dao.OutboxEventDao;
import com.pms.ingestion.Dao.SafeStoreDao;
import com.pms.ingestion.Entity.DlqEntity;
import com.pms.ingestion.Entity.OutboxEventEntity;
import com.pms.ingestion.Entity.SafeStoreEntity;
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

    private final SafeStoreDao safeStoreDao;
    private final OutboxEventDao outboxEventDao;
    private final DlqDao dlqDao;

    @GetMapping("/health")
    @Operation(summary = "Health check")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("OK");
    }

    @GetMapping("/safe-store")
    @Operation(summary = "Get all safe store data")
    public ResponseEntity<List<SafeStoreEntity>> getAllSafeStoreData() {
        List<SafeStoreEntity> safeStoreData = safeStoreDao.findAll();
        return ResponseEntity.ok(safeStoreData);
    }

    @GetMapping("/outbox")
    @Operation(summary = "Get all outbox events")
    public ResponseEntity<List<OutboxEventEntity>> getAllOutboxEvents() {
        List<OutboxEventEntity> outboxEvents = outboxEventDao.findAll();
        return ResponseEntity.ok(outboxEvents);
    }

    @GetMapping("/dlq")
    @Operation(summary = "Get all DLQ events")
    public ResponseEntity<List<DlqEntity>> getAllDlqEvents() {
        List<DlqEntity> dlqEvents = dlqDao.findAll();
        return ResponseEntity.ok(dlqEvents);
    }

}
