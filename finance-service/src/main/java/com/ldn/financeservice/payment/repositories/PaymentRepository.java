package com.ldn.financeservice.payment.repositories;

import com.ldn.financeservice.payment.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
