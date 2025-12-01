package com.pms.ingestion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.ingestion.repository.DlqRepository;
import com.pms.ingestion.repository.OutboxEventRepository;
import com.pms.ingestion.repository.SafeStoreRepository;
import com.pms.ingestion.dto.RawTradeDto;
import com.pms.ingestion.entity.DlqTrade;
import com.pms.ingestion.entity.OutboxTrade;
import com.pms.ingestion.entity.SafeStoreTrade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final ObjectMapper objectMapper;
    private final OutboxEventRepository outboxEventRepository;
    private final SafeStoreRepository safeStoreRepository;
    private final DlqRepository dlqRepository;

    public void processWebsocketMessage(String rawMessage) {

        try {
            RawTradeDto rawTradeDto = objectMapper.readValue(rawMessage, RawTradeDto.class);

            if(rawTradeDto.getPortfolioId() == null || rawTradeDto.getTradeId() == null || rawTradeDto.getTimestamp() == null
                    || rawTradeDto.getSide() == null || rawTradeDto.getPricePerStock() == null || rawTradeDto.getQuantity() == null
                    || rawTradeDto.getSymbol() == null) {
                throw new IllegalArgumentException("Missing one or more required fields");
            }

            SafeStoreTrade safeStoreTrade = new SafeStoreTrade();
            safeStoreTrade.setReceivedAt(Instant.now());
            safeStoreTrade.setPortfolioId(rawTradeDto.getPortfolioId());
            safeStoreTrade.setTradeId(rawTradeDto.getTradeId());
            safeStoreTrade.setSymbol(rawTradeDto.getSymbol());
            safeStoreTrade.setSide(rawTradeDto.getSide());
            safeStoreTrade.setPricePerStock(rawTradeDto.getPricePerStock());
            safeStoreTrade.setQuantity(rawTradeDto.getQuantity());
            safeStoreTrade.setTimestamp(rawTradeDto.getTimestamp());
            safeStoreRepository.save(safeStoreTrade);

            OutboxTrade outboxTrade = new OutboxTrade();
            outboxTrade.setCreatedAt(Instant.now());
            outboxTrade.setPortfolioId(rawTradeDto.getPortfolioId());
            outboxTrade.setTradeId(rawTradeDto.getTradeId());
            outboxTrade.setSymbol(rawTradeDto.getSymbol());
            outboxTrade.setSide(rawTradeDto.getSide());
            outboxTrade.setPricePerStock(rawTradeDto.getPricePerStock());
            outboxTrade.setQuantity(rawTradeDto.getQuantity());
            outboxTrade.setTimestamp(rawTradeDto.getTimestamp());
            outboxEventRepository.save(outboxTrade);

            log.info("Trade data processed and saved to outbox: {}", outboxTrade);


        } catch (Exception ex) {
            log.warn("Error processing trade data: {}", ex.getMessage());

            DlqTrade dlqTrade = new DlqTrade();
            dlqTrade.setFailedAt(Instant.now());
            dlqTrade.setRawMessage(rawMessage);
            dlqTrade.setErrorDetail(ex.getMessage());
            dlqRepository.save(dlqTrade);
        }
    }
}
