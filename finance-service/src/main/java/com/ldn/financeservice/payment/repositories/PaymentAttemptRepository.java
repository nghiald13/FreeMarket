package com.ldn.financeservice.payment.repositories;

import com.ldn.financeservice.payment.entities.PaymentAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttemptRepository extends JpaRepository<PaymentAttempt, Long> {
}
