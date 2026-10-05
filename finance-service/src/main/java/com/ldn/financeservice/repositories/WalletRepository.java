package com.ldn.financeservice.repositories;

import com.ldn.financeservice.entities.PaymentWebhookEvent;
import com.ldn.financeservice.entities.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
}
