package com.ldn.inventoryservice.catalogue.repositories;

import com.ldn.inventoryservice.catalogue.entities.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductImageRepository extends JpaRepository<ProductImage, UUID> {
}
