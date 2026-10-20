package com.ldn.financeservice.wallet.repositories;

import com.ldn.financeservice.wallet.entities.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
}
