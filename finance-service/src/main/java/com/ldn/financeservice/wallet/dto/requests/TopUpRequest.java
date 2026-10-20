package com.ldn.financeservice.wallet.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record TopUpRequest(
        @NotNull
        @Min(value = 10000)
        BigDecimal amount,

        @NotEmpty
        String method
) {
}
