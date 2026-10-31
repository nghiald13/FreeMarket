package com.ldn.inventoryservice.cart.repositories;

import com.ldn.inventoryservice.cart.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<Cart, UUID> {
    Optional<Cart> findByCustomerId(Long customerId);
}
