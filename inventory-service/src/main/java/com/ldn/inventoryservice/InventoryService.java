package com.ldn.inventoryservice;

import com.ldn.inventoryservice.pojo.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;

    public List<Product> getAllProducts() {
        return this.inventoryRepository.findAll();
    }

    public String test() {
        return "Test";
    }
}
