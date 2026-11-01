package com.ldn.inventoryservice.catalogue.dto.request.types;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

public record VariantDto(
        String sku,
        @NotEmpty String name,
        @NotNull BigDecimal price,
        @NotNull BigDecimal costPrice,
        Integer weightGrams,
        Map<String, Object> options,
        @NotNull Boolean isDefault,
        @NotEmpty String imageUrl,
        @NotNull Integer quantityOnHand
) {
}
