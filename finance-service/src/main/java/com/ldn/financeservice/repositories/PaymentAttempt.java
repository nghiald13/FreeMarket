package com.ldn.financeservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentAttempt extends JpaRepository<PaymentAttempt, Long> {
}
