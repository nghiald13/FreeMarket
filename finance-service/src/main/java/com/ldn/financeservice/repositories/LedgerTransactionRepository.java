package com.ldn.financeservice.repositories;

import com.ldn.financeservice.entities.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerTransactionRepository extends JpaRepository<LedgerTransaction, Long> {
}
