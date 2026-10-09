package com.ldn.financeservice.wallet.repositories;

import com.ldn.financeservice.wallet.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
