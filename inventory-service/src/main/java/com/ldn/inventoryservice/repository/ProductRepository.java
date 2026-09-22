package com.ldn.inventoryservice.repository;

import com.ldn.inventoryservice.pojo.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

}
