package com.ldn.financeservice.wallet.repositories;

import com.ldn.financeservice.wallet.entities.Topup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TopupRepository extends JpaRepository<Topup, Long> {
    Optional<Topup> findByProviderRefId(String providerRefId);
}
