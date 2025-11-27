package com.pms.ingestion.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.ingestion.Dao.DlqDao;
import com.pms.ingestion.Dao.OutboxEventDao;
import com.pms.ingestion.Dao.SafeStoreDao;
import com.pms.ingestion.Dto.RawTradeDto;
import com.pms.ingestion.Entity.DlqEntity;
import com.pms.ingestion.Entity.OutboxEventEntity;
import com.pms.ingestion.Entity.SafeStoreEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Slf4j
@Service
@RequiredArgsConstructor
public class IngestionService {

    private final ObjectMapper objectMapper;
    private final OutboxEventDao outboxEventDao;
    private final SafeStoreDao safeStoreDao;
    private final DlqDao dlqDao;

    public void processWebsocketMessage(String rawMessage) {

        try {
            RawTradeDto rawTradeDto = objectMapper.readValue(rawMessage, RawTradeDto.class);

            if(rawTradeDto.getPId() == null || rawTradeDto.getTId() == null || rawTradeDto.getTimestamp() == null
                    || rawTradeDto.getSide() == null || rawTradeDto.getPricePerStock() == null || rawTradeDto.getQuantity() == null
                    || rawTradeDto.getCusipId() == null) {
                throw new IllegalArgumentException("Missing one or more required fields");
            }

            SafeStoreEntity safeStoreEntity = new SafeStoreEntity();
            safeStoreEntity.setReceivedAt(Instant.now());
            safeStoreEntity.setPId(rawTradeDto.getPId());
            safeStoreEntity.setTId(rawTradeDto.getTId());
            safeStoreEntity.setCusipId(rawTradeDto.getCusipId());
            safeStoreEntity.setSide(rawTradeDto.getSide());
            safeStoreEntity.setPricePerStock(rawTradeDto.getPricePerStock());
            safeStoreEntity.setQuantity(rawTradeDto.getQuantity());
            safeStoreEntity.setTimestamp(rawTradeDto.getTimestamp());
            safeStoreDao.save(safeStoreEntity);

            OutboxEventEntity outboxEventEntity = new OutboxEventEntity();
            outboxEventEntity.setCreatedAt(Instant.now());
            outboxEventEntity.setPId(rawTradeDto.getPId());
            outboxEventEntity.setTId(rawTradeDto.getTId());
            outboxEventEntity.setCusipId(rawTradeDto.getCusipId());
            outboxEventEntity.setSide(rawTradeDto.getSide());
            outboxEventEntity.setPricePerStock(rawTradeDto.getPricePerStock());
            outboxEventEntity.setQuantity(rawTradeDto.getQuantity());
            outboxEventEntity.setTimestamp(rawTradeDto.getTimestamp());
            outboxEventDao.save(outboxEventEntity);

            log.info("Trade data processed and saved to outbox: {}", outboxEventEntity);


        } catch (Exception ex) {
            log.warn("Error processing trade data: {}", ex.getMessage());

            DlqEntity dlqEntity = new DlqEntity();
            dlqEntity.setFailedAt(Instant.now());
            dlqEntity.setRawMessage(rawMessage);
            dlqEntity.setErrorDetail(ex.getMessage());
            dlqDao.save(dlqEntity);
        }
    }
}
