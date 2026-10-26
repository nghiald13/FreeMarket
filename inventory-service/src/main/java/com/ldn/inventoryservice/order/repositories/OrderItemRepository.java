package com.ldn.inventoryservice.order.repositories;

import com.ldn.inventoryservice.order.entities.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {
}
