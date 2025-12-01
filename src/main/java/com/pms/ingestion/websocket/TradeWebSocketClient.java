package com.pms.ingestion.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pms.ingestion.entity.TradeEvent;
import com.pms.ingestion.redis.RedisStreamProducer;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Component
public class TradeWebSocketClient {
    private static final Logger log = LoggerFactory.getLogger(TradeWebSocketClient.class);
    private final RedisStreamProducer redisProducer;
    private final ObjectMapper objectMapper;


    @Value("${app.wsSimulatorUrl}")
    private String wsUrl;


    public TradeWebSocketClient(RedisStreamProducer redisProducer) {
        this.redisProducer = redisProducer;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }


    @PostConstruct
    public void start() {
        StandardWebSocketClient client = new StandardWebSocketClient();
        client.doHandshake(new AbstractWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                try {
                    TradeEvent evt = objectMapper.readValue(message.getPayload(), TradeEvent.class);
                    log.info("Received WS: {}", message.getPayload());
                    redisProducer.push(evt);
                } catch (Exception e) {
                    log.error("Failed to parse WS message", e);
                }
            }
        }, wsUrl);


        log.info("Connected to WS: {}", wsUrl);
    }
}