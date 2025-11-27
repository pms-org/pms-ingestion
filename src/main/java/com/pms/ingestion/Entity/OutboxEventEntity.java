package com.pms.ingestion.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;


@Entity
@Table(name = "outbox_trade")
@Data
public class OutboxEventEntity {


    @Id
    @GeneratedValue
    @UuidGenerator
    private String id;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "p_id", nullable = false)
    private String pId;

    @Column(name = "t_id", nullable = false)
    private String tId;

    @Column(name = "cusip_id", nullable = false)
    private String cusipId;

    @Column(name = "side", nullable = false)
    private String side;

    @Column(name = "price_per_stock", nullable = false)
    private Double pricePerStock;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "timestamp", nullable = false)
    private String timestamp;
}
