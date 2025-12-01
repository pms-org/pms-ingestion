package com.pms.ingestion.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pms.ingestion.entity.TradeEvent;
import com.pms.ingestion.service.TransactionalWriter;
import io.lettuce.core.Consumer;
import io.lettuce.core.StreamMessage;
import io.lettuce.core.XReadArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RedisBatchConsumer {
    private static final Logger log = LoggerFactory.getLogger(RedisBatchConsumer.class);
    private final StatefulRedisConnection<String, String> connection;
    private final TransactionalWriter writer;
    private final ObjectMapper mapper;

    {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }


    @Value("${app.redisStreamName:trades}")
    private String streamName;


    @Value("${app.redisGroup:ingestion-group}")
    private String groupName;


    public RedisBatchConsumer(StatefulRedisConnection<String, String> connection, TransactionalWriter writer) {
        this.connection = connection;
        this.writer = writer;
    }

    @PostConstruct
    public void start() {
        RedisCommands<String, String> commands = connection.sync();
        try {
            // MKSTREAM creates the stream if it does not exist
            commands.xgroupCreate(
                    XReadArgs.StreamOffset.from(streamName, "0"),
                    groupName,
                    io.lettuce.core.XGroupCreateArgs.Builder.mkstream()
            );
            log.info("Created consumer group '{}' for stream '{}' (stream created if missing)", groupName, streamName);
        } catch (io.lettuce.core.RedisBusyException e) {
            // Consumer group already exists – this is fine
            log.info("Consumer group '{}' already exists for stream '{}'", groupName, streamName);
        } catch (Exception e) {
            log.warn("Could not create consumer group '{}': {}", groupName, e.getMessage());
        }
        Thread t = new Thread(this::loop);
        t.setDaemon(true);
        t.start();
        log.info("RedisBatchConsumer started, reading from stream: {}", streamName);
    }


    private void loop() {
        RedisCommands<String, String> commands = connection.sync();
        log.info("Starting consumer loop for stream: {} with group: {}", streamName, groupName);
        while (true) {
            try {
                // XREADGROUP GROUP group consumer COUNT 100 BLOCK 2000 STREAMS stream >
                List<StreamMessage<String, String>> messages = commands.xreadgroup(Consumer.from(groupName, "consumer-1"), XReadArgs.Builder.count(200).block(Duration.ofSeconds(2)), XReadArgs.StreamOffset.lastConsumed(streamName));
                if (messages == null || messages.isEmpty()) {
                    continue;
                }

                log.info("Received {} messages from Redis stream", messages.size());
                List<TradeEvent> events = new ArrayList<>();
                List<String> ids = new ArrayList<>();

                for (StreamMessage<String, String> m : messages) {
                    Map<String, String> map = m.getBody();
                    String payload = map.get("payload");
                    TradeEvent evt = mapper.readValue(payload, TradeEvent.class);
                    // Filter out invalid messages with null critical fields
                    if (evt.getTradeId() != null && evt.getSymbol() != null && evt.getSide() != null) {
                        events.add(evt);
                        ids.add(m.getId());
                    } else {
                        log.warn("Skipping invalid trade event with null fields: {}", payload);
                        // Still acknowledge to remove from stream
                        commands.xack(streamName, groupName, m.getId());
                    }
                }
                // write to DB transactionally
                writer.writeBatch(events);
                log.info("Wrote {} events to database", events.size());

                // acknowledge
                ids.forEach(id -> commands.xack(streamName, groupName, id));
                log.info("Acknowledged {} messages", ids.size());


            } catch (Exception e) {
                log.error("Error in redis batch loop", e);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        }
    }
}
