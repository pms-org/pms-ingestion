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
import com.pms.ingestion.service.CrossCuttingService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class TransactionalWriter {
    private final SafeStoreRepository safeRepo;
    private final OutboxEventRepository outboxRepo;
    private final DlqTradeRepository dlqRepo;
    private final CrossCuttingService crossCuttingService;

    public TransactionalWriter(SafeStoreRepository safeRepo, OutboxEventRepository outboxRepo, DlqTradeRepository dlqRepo,CrossCuttingService crossCuttingService) {
        this.safeRepo = safeRepo;
        this.outboxRepo = outboxRepo;
        this.dlqRepo = dlqRepo;
        this.crossCuttingService = crossCuttingService;
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

                log.info("Calling crossCuttingService.recordIngestionSuccess for trade: {}", e.getTradeId());
                crossCuttingService.recordIngestionSuccess(e, ss, ob);
            } catch (Exception ex) {
                // Store invalid trade in DLQ
                DlqTrade dlq = new DlqTrade();
                dlq.setFailedAt(LocalDateTime.now());
                dlq.setRawMessage(e.toString());
                dlq.setErrorDetail(ex.getMessage());
                dlqRepo.save(dlq);

                crossCuttingService.recordIngestionFailure(e, dlq, ex);
            }
        }
    }
}


