package com.pms.ingestion.repository;

import com.pms.ingestion.entity.DlqTrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DlqRepository extends JpaRepository<DlqTrade, UUID> {
}
