package com.ldn.financeservice.repositories;

import com.ldn.financeservice.entities.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
}
