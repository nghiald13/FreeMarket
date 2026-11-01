package com.ldn.inventoryservice.catalogue.repositories;

import com.ldn.inventoryservice.catalogue.entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MerchantProductRepository extends JpaRepository<Product, UUID> {
    Page<Product> findAllByMerchantId(UUID merchantId, Pageable pageable);
}
