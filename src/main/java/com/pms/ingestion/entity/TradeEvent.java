package com.pms.ingestion.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TradeEvent {
    private UUID portfolioId;
    private UUID tradeId;
    private String symbol;
    private String side;
    private double pricePerStock;
    private long quantity;
    private LocalDateTime timestamp;
}
