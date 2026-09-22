package com.ldn.inventoryservice;

import com.ldn.inventoryservice.pojo.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryRepository extends JpaRepository<Product, Long> {

}
