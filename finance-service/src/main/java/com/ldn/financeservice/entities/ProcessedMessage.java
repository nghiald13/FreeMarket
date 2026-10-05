package com.ldn.financeservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "processed_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessedMessage {
    @Id
    @Column(name = "message_id", length = 100, nullable = false)
    private String messageId;

    @Column(name = "consumer", length = 50, nullable = false)
    private String consumer;

    @Column(name = "processed_at", updatable = false)
    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        processedAt = LocalDateTime.now();
    }
}
