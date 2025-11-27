package com.pms.ingestion.Dao;

import com.pms.ingestion.Entity.SafeStoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SafeStoreDao extends JpaRepository<SafeStoreEntity, String> {
}
