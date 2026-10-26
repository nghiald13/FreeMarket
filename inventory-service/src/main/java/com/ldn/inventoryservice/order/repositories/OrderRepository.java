package com.ldn.inventoryservice.order.repositories;

import com.ldn.inventoryservice.order.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
