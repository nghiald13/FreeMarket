package com.ldn.financeservice.ledger.repositories;

import com.ldn.financeservice.ledger.entities.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
}
