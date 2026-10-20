package com.ldn.financeservice.wallet.entities;

import com.ldn.financeservice.wallet.enums.LedgerEntryDirection;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ledger_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LedgerEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private LedgerTransaction ledgerTransaction;

    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", length = 6, nullable = false)
    private LedgerEntryDirection direction;

    @Column(name = "amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "balance_after", precision = 19, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public LedgerEntry credit(Wallet wallet, LedgerTransaction ledgerTransaction) {
        this.direction = LedgerEntryDirection.CREDIT;
        this.ledgerTransaction = ledgerTransaction;
        this.wallet = wallet;
        this.amount = ledgerTransaction.getAmount();
        this.balanceAfter = wallet.getBalance();
        return this;
    }

    public LedgerEntry debit(Wallet wallet, LedgerTransaction ledgerTransaction) {
        this.direction = LedgerEntryDirection.DEBIT;
        this.ledgerTransaction = ledgerTransaction;
        this.wallet = wallet;
        this.amount = ledgerTransaction.getAmount();
        this.balanceAfter = wallet.getBalance();
        return this;
    }
}