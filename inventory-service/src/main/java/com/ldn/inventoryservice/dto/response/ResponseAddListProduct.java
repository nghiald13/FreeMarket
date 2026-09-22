package com.ldn.inventoryservice.dto.response;

import com.ldn.inventoryservice.pojo.Product;

public record ResponseAddListProduct(Long id, String name, Integer quantity, Long price) {
    public static ResponseAddListProduct fromEntity(Product product) {
        return new ResponseAddListProduct(product.getId(), product.getName(), product.getQuantity(), product.getPrice());
    }
}
