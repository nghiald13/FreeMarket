package com.ldn.inventoryservice.catalogue.repositories;

import com.ldn.inventoryservice.catalogue.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
