package com.pms.ingestion.Dao;

import com.pms.ingestion.Entity.DlqEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DlqDao extends JpaRepository<DlqEntity, String> {
}
