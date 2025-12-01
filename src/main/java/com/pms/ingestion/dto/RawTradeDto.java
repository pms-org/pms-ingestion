package com.pms.ingestion.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;


@Data
public class RawTradeDto {


    @JsonProperty("portfolio_id")
    private UUID portfolioId;

    @JsonProperty("trade_id")
    private UUID tradeId;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("side")
    private String side;          // BUY / SELL

    @JsonProperty("price_per_stock")
    private Double pricePerStock;

    @JsonProperty("quantity")
    private Long quantity;

    @JsonProperty("timestamp")
    private LocalDateTime timestamp;

}
