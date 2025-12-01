package com.pms.ingestion.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;


@Entity
@Table(name = "outbox_trade")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OutboxTrade {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

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

    @Column(name = "status", nullable = false)
    private String status = "PENDING";

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0;

    //! replace with more scaleable code for production use
    public String toJson() {
        return String.format("{\"portfolioId\":\"%s\",\"tradeId\":\"%s\",\"symbol\":\"%s\",\"side\":\"%s\",\"pricePerStock\":%s,\"quantity\":%d,\"timestamp\":\"%s\"}",
                portfolioId, tradeId, symbol, side, pricePerStock, quantity, timestamp.toString());
    }
}
