package com.pms.ingestion.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;

@Entity
@Table(name = "dlq_trade")
@Data
public class DlqEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    @Column(name = "failed_at", nullable = false)
    private Instant failedAt;

    @Lob
    @Column(name = "raw_message", nullable = false)
    private String rawMessage;

    @Column(name = "error_detail")
    private String errorDetail;
}
