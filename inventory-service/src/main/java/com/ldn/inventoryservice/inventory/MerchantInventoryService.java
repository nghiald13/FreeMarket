package com.ldn.inventoryservice.inventory;

import com.ldn.inventoryservice.catalogue.entities.ProductVariant;
import com.ldn.inventoryservice.inventory.entities.StockItem;
import com.ldn.inventoryservice.inventory.entities.StockMovement;
import com.ldn.inventoryservice.inventory.entities.Warehouse;
import com.ldn.inventoryservice.inventory.repositories.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MerchantInventoryService {
    private final WarehouseRepository warehouseRepository;

    @Transactional
    public void addStockItems(UUID merchantId, Map<ProductVariant, Integer> stockItems) {
        Warehouse warehouse = this.warehouseRepository.findByMerchantId(merchantId);
        stockItems.forEach((productVariant, quantity) -> {
            StockItem stockItem = new StockItem();
            stockItem.setWarehouse(warehouse);
            stockItem.setProductVariant(productVariant);
            stockItem.receive(quantity);
            warehouse.addStockItem(stockItem);
        } );

        this.warehouseRepository.save(warehouse);
    }

}
