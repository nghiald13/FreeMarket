package com.ldn.financeservice.repositories;

import com.ldn.financeservice.entities.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
}
