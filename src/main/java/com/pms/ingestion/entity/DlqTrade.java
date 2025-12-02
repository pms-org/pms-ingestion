package com.pms.ingestion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "dlq_trade")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DlqTrade {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dlq_trade_seq")
    @SequenceGenerator(name = "dlq_trade_seq", sequenceName = "dlq_trade_id_seq", allocationSize = 1)
    private Long id;
    
    @Column(name = "error_detail")
    private String errorDetail;
    
    @Column(name = "failed_at")
    private LocalDateTime failedAt;
    
    @Column(name = "raw_message", columnDefinition = "TEXT")
    private String rawMessage;
}