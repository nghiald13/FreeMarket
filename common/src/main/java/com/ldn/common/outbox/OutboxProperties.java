package com.ldn.common.outbox;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.outbox")
public class OutboxProperties {
    private boolean enabled = true;
    private String serviceName = "unknown";
    private int batchSize = 50;
    private int maxLoopsPerRun = 10;
    private int retentionDays = 7;
}
