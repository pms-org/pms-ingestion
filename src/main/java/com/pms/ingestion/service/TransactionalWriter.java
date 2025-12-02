package com.pms.ingestion.service;

import com.pms.ingestion.entity.DlqTrade;
import com.pms.ingestion.entity.OutboxTrade;
import com.pms.ingestion.entity.SafeStoreTrade;
import com.pms.ingestion.entity.TradeEvent;
import com.pms.ingestion.repository.DlqTradeRepository;
import com.pms.ingestion.repository.OutboxEventRepository;
import com.pms.ingestion.repository.SafeStoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;


@Service
public class TransactionalWriter {
    private final SafeStoreRepository safeRepo;
    private final OutboxEventRepository outboxRepo;
    private final DlqTradeRepository dlqRepo;

    public TransactionalWriter(SafeStoreRepository safeRepo, OutboxEventRepository outboxRepo, DlqTradeRepository dlqRepo) {
        this.safeRepo = safeRepo;
        this.outboxRepo = outboxRepo;
        this.dlqRepo = dlqRepo;
    }


    @Transactional
    public void writeBatch(List<TradeEvent> events) {
        for (TradeEvent e : events) {
            try {
                SafeStoreTrade ss = new SafeStoreTrade();
                ss.setReceivedAt(Instant.now());
                ss.setPortfolioId(e.getPortfolioId());
                ss.setTradeId(e.getTradeId());
                ss.setSymbol(e.getSymbol());
                ss.setSide(e.getSide());
                ss.setPricePerStock(e.getPricePerStock());
                ss.setQuantity(e.getQuantity());
                ss.setTimestamp(e.getTimestamp());
                safeRepo.save(ss);

                OutboxTrade ob = new OutboxTrade();
                ob.setCreatedAt(Instant.now());
                ob.setPortfolioId(e.getPortfolioId());
                ob.setTradeId(e.getTradeId());
                ob.setSymbol(e.getSymbol());
                ob.setSide(e.getSide());
                ob.setPricePerStock(e.getPricePerStock());
                ob.setQuantity(e.getQuantity());
                ob.setTimestamp(e.getTimestamp());
                outboxRepo.save(ob);
            } catch (Exception ex) {
                // Store invalid trade in DLQ
                DlqTrade dlq = new DlqTrade();
                dlq.setFailedAt(LocalDateTime.now());
                dlq.setRawMessage(e.toString());
                dlq.setErrorDetail(ex.getMessage());
                dlqRepo.save(dlq);
            }
        }
    }
}


