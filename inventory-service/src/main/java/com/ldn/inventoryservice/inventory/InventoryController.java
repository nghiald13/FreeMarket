package com.ldn.inventoryservice.inventory;

import com.ldn.inventoryservice.catalogue.CatalogueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;


}
