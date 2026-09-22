package com.ldn.inventoryservice.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CartRequest(
        @Valid
        @NotEmpty(message = "No items in cart")
        List<CartItem> items
) {
    public record CartItem(
            @NotNull(message = "Product ID is required")
            Long productId,

            @NotNull
            @Min(value = 1, message = "Item quantity must be greater than or equal 1")
            Integer quantity
    ) {}
}
