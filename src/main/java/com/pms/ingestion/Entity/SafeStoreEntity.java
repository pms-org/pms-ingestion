package com.pms.ingestion.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "safe_store_trade")
@Data
public class SafeStoreEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    @Column(name = "received_at", nullable = false)
    private Instant receivedAt;

    @Column(name = "portfolio_id", nullable = false)
    private UUID portfolioId;

    @Column(name = "trade_id", nullable = false)
    private UUID tradeId;

    @Column(name = "symbol", nullable = false)
    private String symbol;

    @Column(name = "side", nullable = false)
    private String side;

    @Column(name = "price_per_stock", nullable = false)
    private Double pricePerStock;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
}
