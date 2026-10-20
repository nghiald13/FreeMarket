package com.ldn.financeservice.zalopay.dto.requests.types;

import lombok.Builder;

@Builder
public record ZaloPayCallbackData(
        int app_id,
        String app_trans_id,
        Long app_time,
        String app_user,
        Long amount,
        String embed_data,
        String item,
        Long zp_trans_id,
        Long server_time,
        int channel,
        String merchant_user_id,
        Long user_fee_amount,
        Long discount_amount
) {
}
