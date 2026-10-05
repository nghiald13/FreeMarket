package com.ldn.financeservice.repositories;

import com.ldn.financeservice.entities.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
}
