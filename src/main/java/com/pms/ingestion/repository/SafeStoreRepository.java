package com.pms.ingestion.repository;

import com.pms.ingestion.entity.SafeStoreTrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SafeStoreRepository extends JpaRepository<SafeStoreTrade, UUID> {
}
