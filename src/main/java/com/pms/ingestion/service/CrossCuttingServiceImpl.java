package com.pms.ingestion.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pms.ingestion.entity.DlqTrade;
import com.pms.ingestion.entity.OutboxTrade;
import com.pms.ingestion.entity.SafeStoreTrade;
import com.pms.ingestion.entity.TradeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrossCuttingServiceImpl implements CrossCuttingService {
    private static final String LIFECYCLE_TOPIC = "lifecycle.event";
    private static final String SERVICE_NAME = "PMS_INGESTION";
    private static final String STAGE_INGESTION = "INGESTION";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void recordIngestionSuccess(TradeEvent tradeEvent,
                                       SafeStoreTrade safeStoreTrade,
                                       OutboxTrade outboxTrade) {

        Map<String, Object> details = new HashMap<>();
        details.put("sourceService", SERVICE_NAME);
        details.put("eventType", "INGESTION_PERSISTED");
        details.put("safeStoreId", safeStoreTrade.getId());
        details.put("outboxId", outboxTrade.getId());
        details.put("receivedAt", safeStoreTrade.getReceivedAt());
        details.put("createdAt", outboxTrade.getCreatedAt());

        sendLifecycleEvent(
                tradeEvent,
                STAGE_INGESTION,
                "SUCCESS",
                details
        );
    }

    @Override
    public void recordIngestionFailure(TradeEvent tradeEvent,
                                       DlqTrade dlqTrade,
                                       Exception ex) {

        Map<String, Object> details = new HashMap<>();
        details.put("sourceService", SERVICE_NAME);
        details.put("eventType", "INGESTION_FAILED");
        details.put("dlqId", dlqTrade.getId());
        details.put("failedAt", dlqTrade.getFailedAt());
        details.put("errorMessage", ex.getMessage());
        details.put("exceptionType", ex.getClass().getName());

        sendLifecycleEvent(
                tradeEvent,
                STAGE_INGESTION,
                "FAILURE",
                details
        );
    }

    private void sendLifecycleEvent(TradeEvent tradeEvent,
                                    String stage,
                                    String status,
                                    Map<String, Object> details) {

        Map<String, Object> payload = new HashMap<>();
        payload.put("traceId", tradeEvent.getTradeId());      // or a dedicated correlationId// if you have one; else null
        payload.put("portfolioId", tradeEvent.getPortfolioId());
        payload.put("stage", stage);
        payload.put("status", status);
        payload.put("ts", Instant.now());
        payload.put("details", details);

        try {
            String json = objectMapper.writeValueAsString(payload);

            // Use tradeId as kafka key to keep ordering
            String key = tradeEvent.getTradeId().toString();
            
            log.info("About to send message to Kafka - Topic: {}, Key: {}, Message: {}", LIFECYCLE_TOPIC, key, json);
            try {
                kafkaTemplate.send(LIFECYCLE_TOPIC, key, json).get();
                log.info("Successfully sent lifecycle.event to topic '{}': key={}, status={}, stage={}",
                        LIFECYCLE_TOPIC, key, status, stage);
            } catch (Exception kafkaEx) {
                log.error("Failed to send lifecycle.event to topic '{}': key={}, error: {}",
                        LIFECYCLE_TOPIC, key, kafkaEx.getMessage(), kafkaEx);
                throw kafkaEx;
            }

        } catch (JsonProcessingException e) {
            log.error("Failed to serialize lifecycle event for trade {}", tradeEvent.getTradeId(), e);
        } catch (Exception e) {
            log.error("Failed to send lifecycle.event to Kafka for trade {} - Error: {}", tradeEvent.getTradeId(), e.getMessage(), e);
        }
    }
}
