package com.ldn.financeservice.wallet.repositories;

import com.ldn.financeservice.wallet.entities.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {
}
