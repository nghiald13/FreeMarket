package com.ldn.financeservice.payment.repositories;

import com.ldn.financeservice.payment.entities.PaymentWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentWebhookEventRepository extends JpaRepository<PaymentWebhookEvent, Long> {
}
