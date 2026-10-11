package com.ldn.financeservice.wallet.repositories;

import com.ldn.financeservice.wallet.entities.Wallet;
import com.ldn.financeservice.wallet.enums.WalletType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByAccountIdAndWalletType(Long accountId, WalletType walletType);
}
