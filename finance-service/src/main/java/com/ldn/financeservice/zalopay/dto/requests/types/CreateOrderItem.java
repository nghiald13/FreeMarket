package com.ldn.financeservice.zalopay.dto.requests.types;

import lombok.Builder;

@Builder
public record CreateOrderItem(
        String itemId,
        String itemName,
        String itemPrice,
        String itemQuantity
) {
}
