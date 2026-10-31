package com.ldn.inventoryservice.catalogue.dto.request;

import com.ldn.inventoryservice.catalogue.dto.request.types.VariantDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record AddProductRequest(
        @NotNull UUID brandId,
        @NotNull UUID categoryId,
        @NotBlank String name,
        @NotBlank String description,
        @NotEmpty Map<String, Object> attributes,
        @NotEmpty List<@Valid VariantDto> variants
) {
}
