package com.ldn.common.outbox;

import com.ldn.common.outbox.repositories.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
public class OutboxPublisherJob {

    private final OutboxBatchProcessor processor;
    private final OutboxEventRepository repository;
    private final OutboxProperties props;

    @Scheduled(cron = "${app.outbox.publisher-cron:*/2 * * * * *}")
    public void publishPending() {
        int loops = 0;
        int published;
        do {
            published = processor.processBatch();
            if (published > 0) log.debug("Outbox published {} events", published);
        } while (published >= props.getBatchSize() && ++loops < props.getMaxLoopsPerRun());
    }

    @Scheduled(cron = "${app.outbox.cleanup-cron:0 0 3 * * *}")
    @Transactional
    public void cleanup() {
        LocalDateTime before = LocalDateTime.now().minus(Duration.ofDays(props.getRetentionDays()));
        int deleted = repository.deletePublishedBefore(before);
        log.info("Outbox cleanup removed {} published events", deleted);
    }
}