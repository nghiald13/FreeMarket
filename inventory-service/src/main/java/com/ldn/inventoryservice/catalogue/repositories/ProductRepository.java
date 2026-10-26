package com.ldn.inventoryservice.catalogue.repositories;

import com.ldn.inventoryservice.catalogue.entities.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {

}
