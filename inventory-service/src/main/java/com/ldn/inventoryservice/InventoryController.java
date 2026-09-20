package com.ldn.inventoryservice;

import com.ldn.inventoryservice.pojo.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return this.inventoryService.getAllProducts();
    }

    @GetMapping("/test")
    public String test() {
        return this.inventoryService.test();
    }
}
