package com.pms.ingestion.Dao;

import com.pms.ingestion.Entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventDao extends JpaRepository<OutboxEventEntity, String> {
}
