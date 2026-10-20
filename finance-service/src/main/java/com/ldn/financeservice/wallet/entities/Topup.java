package com.ldn.financeservice.wallet.entities;

import com.ldn.financeservice.wallet.enums.TopupStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "topups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Topup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "method", nullable = false)
    private String method;

    @Column(name = "provider")
    private String provider;

    @Column(name = "provider_ref_id", nullable = false, unique = true)
    private String providerRefId;

    @Column(name = "provider_txn_id", unique = true)
    private String providerTxnId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private TopupStatus status = TopupStatus.PENDING;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_transaction_id")
    private LedgerTransaction ledgerTransaction;

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

    public static final String PROVIDER_REF_ID_PREFIX = "TUW";

    public Topup withZaloPay(String providerRefId) {
        this.provider = "ZALOPAY";
        this.method = "ZALOPAY GATEWAY";
        this.providerRefId = providerRefId;
        return this;
    }

    public Topup markSucceeded(String providerTxnId, LedgerTransaction ledgerTransaction) {
        this.providerTxnId = providerTxnId;
        this.ledgerTransaction = ledgerTransaction;
        this.status = TopupStatus.SUCCESS;
        return this;
    }

    public Topup markFailed() {
        this.status = TopupStatus.FAILED;
        return this;
    }
}
