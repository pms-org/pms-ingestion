package com.pms.ingestion.Dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;


@Data
public class RawTradeDto {


    @JsonProperty("p_id")
    private String pId;

    @JsonProperty("t_id")
    private String tId;

    @JsonProperty("cusip_id")
    private String cusipId;

    @JsonProperty("side")
    private String side;          // BUY / SELL

    @JsonProperty("price_per_stock")
    private Double pricePerStock;

    @JsonProperty("quantity")
    private Integer quantity;

    @JsonProperty("timestamp")
    private String timestamp;

}
