package com.ldn.financeservice.wallet.repositories;

import com.ldn.financeservice.wallet.entities.Withdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WithdrawalRepository extends JpaRepository<Withdrawal, Long> {
}
