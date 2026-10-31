package com.ldn.inventoryservice.cart.dto.responses;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

public record CartItemResponse(
        UUID cartItemId,
        UUID variantId,
        String productName,
        String variantName,
        String variantImage,
        String variantImageAltText,
        BigDecimal price,
        BigDecimal compareAtPrice,
        Integer quantity
) {
}
