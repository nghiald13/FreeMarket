package com.ldn.inventoryservice.inventory;

import com.ldn.inventoryservice.inventory.entities.Warehouse;
import com.ldn.inventoryservice.inventory.repositories.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private WarehouseRepository warehouseRepository;

    public void createWarehouse(UUID merchantId, String code, String name, String address) {
        this.warehouseRepository.save(new Warehouse()
                .createForMerchant(merchantId, code, name, address)
        );
    }
}
