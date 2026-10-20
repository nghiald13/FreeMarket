package com.ldn.common.outbox;

import com.ldn.common.outbox.entities.OutboxEvent;
import com.ldn.common.outbox.repositories.OutboxEventRepository;
import com.ldn.common.rabbitmq.RabbitMQEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class OutboxBatchProcessor {

    private final OutboxEventRepository repository;
    private final RabbitMQEventPublisher publisher;
    private final OutboxProperties props;

    /** @return số event đã publish thành công trong batch */
    @Transactional
    public int processBatch() {
        List<OutboxEvent> events = repository.findUnpublishedForUpdate(PageRequest.of(0, props.getBatchSize()));
        int success = 0;

        for (OutboxEvent e : events) {
            try {
                String messageId = "%s:%s_%s".formatted(props.getServiceName(), e.getAggregateType(), e.getAggregateId());
                publisher.publishRaw(e.getEventType(), messageId, e.getPayload(), Map.of(
                        OutboxHeader.OUTBOX_ID, e.getId(),
                        OutboxHeader.AGGREGATE_TYPE, e.getAggregateType(),
                        OutboxHeader.AGGREGATE_ID, e.getAggregateId()
                ));
                e.markPublished();      // dirty checking => UPDATE khi commit
                success++;
            } catch (Exception ex) {
                // Dừng batch để giữ thứ tự; các event đã thành công vẫn được commit
                log.warn("Publish outbox event id={} failed: {}", e.getId(), ex.getMessage());
                break;
            }
        }
        return success;
    }
}