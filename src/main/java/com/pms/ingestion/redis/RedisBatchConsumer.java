package com.pms.ingestion.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RedisBatchConsumer {
    private static final Logger log = LoggerFactory.getLogger(RedisBatchConsumer.class);
    private final StatefulRedisConnection<String, String> connection;
    private final TransactionalWriter writer;
    private final ObjectMapper mapper = new ObjectMapper();


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
            commands.xgroupCreate(XReadArgs.StreamOffset.from(streamName, "0"), groupName);
        } catch (Exception e) {
            log.info("consumer group probably exists: {}", e.getMessage());
        }


        Thread t = new Thread(this::loop);
        t.setDaemon(true);
        t.start();
    }


    private void loop() {
        RedisCommands<String, String> commands = connection.sync();
        while (true) {
            try {
                // XREADGROUP GROUP group consumer COUNT 100 BLOCK 2000 STREAMS stream >
                List<StreamMessage<String, String>> messages = commands.xreadgroup(Consumer.from(groupName, "consumer-1"), XReadArgs.Builder.count(200).block(Duration.ofSeconds(2)), XReadArgs.StreamOffset.lastConsumed(streamName));
                if (messages == null || messages.isEmpty()) continue;
                List<TradeEvent> events = new ArrayList<>();
                List<String> ids = new ArrayList<>();
                for (StreamMessage<String, String> m : messages) {
                    Map<String, String> map = m.getBody();
                    String payload = map.get("payload");
                    TradeEvent evt = mapper.readValue(payload, TradeEvent.class);
                    events.add(evt);
                    ids.add(m.getId());
                }
                // write to DB transactionally
                writer.writeBatch(events);

                // acknowledge
                ids.forEach(id -> commands.xack(streamName, groupName, id));


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
