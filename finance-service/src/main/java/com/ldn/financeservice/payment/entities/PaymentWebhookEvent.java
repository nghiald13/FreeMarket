package com.ldn.financeservice.payment.entities;

import com.ldn.financeservice.payment.enums.PaymentProvider;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

@Entity
@Table(name = "payment_webhook_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentWebhookEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 30, nullable = false)
    PaymentProvider provider;

    @Column(name = "event_id", length = 100, nullable = false)
    private String eventId;

    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload", columnDefinition = "jsonb", nullable = false)
    private String payload;

    @Column(name = "signature_valid", nullable = false)
    @Builder.Default
    private boolean signatureValid = false;

    @Column(name = "processed", nullable = false)
    @Builder.Default
    private boolean processed = false;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "received_at", updatable = false)
    private LocalDateTime receivedAt;

    @PrePersist
    protected void onCreate() {
        receivedAt = LocalDateTime.now();
    }
}
