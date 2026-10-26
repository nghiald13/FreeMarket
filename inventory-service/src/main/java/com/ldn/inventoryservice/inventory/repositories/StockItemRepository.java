package com.ldn.inventoryservice.inventory.repositories;

import com.ldn.inventoryservice.inventory.entities.StockItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StockItemRepository extends JpaRepository<StockItem, UUID> {
}
