package com.ldn.financeservice.payment.repositories;

import com.ldn.financeservice.payment.entities.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundRepository extends JpaRepository<Refund, Long> {
}
