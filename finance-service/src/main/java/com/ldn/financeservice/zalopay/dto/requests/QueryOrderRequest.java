package com.ldn.financeservice.zalopay.dto.requests;

import lombok.Builder;

@Builder
public record QueryOrderRequest(
        int app_id,
        String app_trans_id,
        String mac
) {
}
