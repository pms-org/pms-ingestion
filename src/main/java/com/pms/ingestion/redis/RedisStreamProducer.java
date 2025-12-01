package com.pms.ingestion.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pms.ingestion.entity.TradeEvent;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class RedisStreamProducer {
    private static final Logger log = LoggerFactory.getLogger(RedisStreamProducer.class);
    private final StatefulRedisConnection<String, String> connection;
    private final ObjectMapper mapper;


    @Value("${app.redisStreamName:trades}")
    private String streamName;


    public RedisStreamProducer(StatefulRedisConnection<String, String> connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }


    public void push(TradeEvent event) {
        try {
            RedisCommands<String, String> cmd = connection.sync();
            String payload = mapper.writeValueAsString(event);
// XADD stream * payload <json>
            cmd.xadd(streamName, "payload", payload);
        } catch (Exception e) {
            log.error("Failed to push to redis stream", e);
        }
    }
}
