package com.ldn.financeservice.payment.entities;

import com.ldn.financeservice.payment.enums.PaymentMethod;
import com.ldn.financeservice.payment.enums.PaymentProvider;
import com.ldn.financeservice.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_no", length = 40, nullable = false, unique = true)
    private String paymentNo;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "refunded_amount", precision = 19, scale = 2, nullable = false)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(name = "currency", length = 3, nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 30, nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", length = 30)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 30, nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "provider_ref_id", length = 100, nullable = false, unique = true)
    private String providerRefId;

    @Column(name = "provider_txn_id", length = 100)
    private String providerTxnId;

    @Column(name = "failure_code", length = 50)
    private String failureCode;

    @Column(name = "failure_message", length = 255)
    private String failureMessage;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToOne(mappedBy = "payment", cascade = CascadeType.ALL)
    private PaymentAttempt paymentAttempt;

    public void markPaid(String providerTxnId) {
        this.providerTxnId = providerTxnId;
        this.paidAt = LocalDateTime.now();
        this.status = PaymentStatus.PAID;
    }

    public void markCancelled(String providerTxnId, String failureCode, String failureMessage) {
        this.providerTxnId = providerTxnId;
        this.failureCode = failureCode;
        this.failureMessage = failureMessage;
        this.status = PaymentStatus.CANCELLED;
    }

    public void markRefunded(BigDecimal refundedAmount) {
        this.refundedAmount = refundedAmount;
        this.status = PaymentStatus.REFUNDED;
    }
}
