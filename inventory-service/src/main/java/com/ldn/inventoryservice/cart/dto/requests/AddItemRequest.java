package com.ldn.inventoryservice.cart.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddItemRequest(
        @NotNull UUID variantId,
        @NotNull Integer quantity
) {
}
