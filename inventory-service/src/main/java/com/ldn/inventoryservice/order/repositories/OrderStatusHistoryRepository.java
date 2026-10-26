package com.ldn.inventoryservice.order.repositories;

import com.ldn.inventoryservice.order.entities.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, UUID> {
}
