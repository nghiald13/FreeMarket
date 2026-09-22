package com.ldn.inventoryservice.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record RequestListProduct (
        @NotEmpty
        List<RequestProduct> products
) {}
