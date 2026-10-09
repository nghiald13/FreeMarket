package com.ldn.financeservice.ledger.repositories;

import com.ldn.financeservice.ledger.entities.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {
}
