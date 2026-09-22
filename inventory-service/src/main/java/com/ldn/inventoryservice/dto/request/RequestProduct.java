package com.ldn.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record RequestProduct(
        @NotEmpty
        String name,

        @NotNull
        @Min(0)
        Long price,

        @NotNull
        @Min(0)
        Integer quantity
) {}
