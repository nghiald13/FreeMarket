package com.ldn.inventoryservice.dto.response;

import com.ldn.inventoryservice.pojo.Product;

public record CartDetailResponse(Long productId, String name, Long price, Integer quantity) {
    public static CartDetailResponse fromEntity(Product product, Integer quantityRequest) {
        return new CartDetailResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                quantityRequest
        );
    }
}
