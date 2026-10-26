package com.ldn.inventoryservice.inventory.repositories;

import com.ldn.inventoryservice.inventory.entities.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {
}
