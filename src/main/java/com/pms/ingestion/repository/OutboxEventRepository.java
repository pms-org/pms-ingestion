package com.pms.ingestion.repository;

import com.pms.ingestion.entity.OutboxTrade;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxTrade, UUID> {
    @Query(value = "SELECT * FROM outbox_trade WHERE status = 'PENDING' ORDER BY created_at LIMIT 100", nativeQuery = true)
    List<OutboxTrade> findPendingTop100();


    @Modifying
    @Transactional
    @Query(value = "UPDATE outbox_trade SET status = 'SENT' WHERE id = :id", nativeQuery = true)
    void markSent(UUID id);


    @Modifying
    @Transactional
    @Query(value = "UPDATE outbox_trade SET attempts = attempts + 1 WHERE id = :id", nativeQuery = true)
    void incrementAttempts(UUID id);
}
