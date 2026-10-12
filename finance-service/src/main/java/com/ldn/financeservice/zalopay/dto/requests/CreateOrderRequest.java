package com.ldn.financeservice.zalopay.dto.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
public record CreateOrderRequest(
        int app_id,
        String app_user,
        String app_trans_id,
        Long app_time,
        Long expire_duration_seconds,
        Long amount,
        String item,
        String description,
        String embed_data,
        String mac,
        String callback_url
) {
}
