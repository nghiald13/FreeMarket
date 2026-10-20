package com.ldn.common.outbox;

import com.ldn.common.outbox.repositories.OutboxEventRepository;
import com.ldn.common.rabbitmq.RabbitMQEventPublisher;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import tools.jackson.databind.ObjectMapper;

@AutoConfiguration
@AutoConfigurationPackage // đăng ký package này cho entity scan + JPA repository scan
@EnableScheduling
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxAutoConfig {

    @Bean
    public OutboxService outboxService(OutboxEventRepository repo, ObjectMapper mapper) {
        return new OutboxService(repo, mapper);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OutboxBatchProcessor outboxBatchProcessor(OutboxEventRepository repo,
                                                     RabbitMQEventPublisher publisher,
                                                     OutboxProperties props) {
        return new OutboxBatchProcessor(repo, publisher, props);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.outbox", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OutboxPublisherJob outboxPublisherJob(OutboxBatchProcessor processor,
                                                 OutboxEventRepository repo,
                                                 OutboxProperties props) {
        return new OutboxPublisherJob(processor, repo, props);
    }
}
