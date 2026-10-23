package com.ldn.inventoryservice.catalogue.repositories;

import com.ldn.inventoryservice.catalogue.entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BrandRepository extends JpaRepository<Brand, UUID> {
}
