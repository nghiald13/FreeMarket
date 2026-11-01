package com.ldn.inventoryservice.inventory.repositories;

import com.ldn.inventoryservice.inventory.entities.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Warehouse findByMerchantId(UUID merchantId);
}
