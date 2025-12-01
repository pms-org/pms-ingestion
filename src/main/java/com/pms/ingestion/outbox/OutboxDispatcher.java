package com.pms.ingestion.outbox;


import com.pms.ingestion.entity.OutboxTrade;
import com.pms.ingestion.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;


import jakarta.annotation.PostConstruct;
import java.util.List;

@Component
public class OutboxDispatcher {
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);
    private final OutboxEventRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;


    public OutboxDispatcher(OutboxEventRepository outboxRepository, KafkaTemplate<String, String> kafkaTemplate) {
        this.outboxRepository = outboxRepository;
        this.kafkaTemplate = kafkaTemplate;
    }


    @PostConstruct
    public void start() {
        Thread t = new Thread(this::loop);
        t.setDaemon(true);
        t.start();
    }


    private void loop() {
        while (true) {
            try {
                List<OutboxTrade> pending = outboxRepository.findPendingTop100();
                if (pending.isEmpty()) {
                    Thread.sleep(500);
                    continue;
                }


                for (OutboxTrade o : pending) {
                    try {
                        kafkaTemplate.send("raw-trades", o.getPortfolioId().toString(), o.toJson()).get();
                        outboxRepository.markSent(o.getId());
                    } catch (Exception ex) {
                        log.error("publish failed for outbox {}", o.getId(), ex);
                        outboxRepository.incrementAttempts(o.getId());
                    }
                }


            } catch (Exception e) {
                log.error("outbox dispatcher loop error", e);
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }
}
