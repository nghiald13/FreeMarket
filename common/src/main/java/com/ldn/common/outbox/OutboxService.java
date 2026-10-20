package com.ldn.common.outbox;

import com.ldn.common.outbox.entities.OutboxEvent;
import com.ldn.common.outbox.repositories.OutboxEventRepository;
import com.ldn.common.rabbitmq.DomainEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
public class OutboxService {
    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    /** MANDATORY: bắt buộc gọi trong transaction nghiệp vụ, tránh quên @Transactional */
    @Transactional(propagation = Propagation.MANDATORY)
    public void record(String aggregateType, String aggregateId, DomainEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .aggregateType(aggregateType)
                .aggregateId(aggregateId)
                .eventType(event.routingKey())
                .payload(payload)
                .build();
        repository.save(outboxEvent);
    }

    public boolean existsByAggregateId(String aggregateId) {
        return this.repository.existsByAggregateId(aggregateId);
    }
}
