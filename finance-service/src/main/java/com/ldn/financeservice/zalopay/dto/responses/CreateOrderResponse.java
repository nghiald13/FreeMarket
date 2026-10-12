package com.ldn.financeservice.zalopay.dto.responses;

public record CreateOrderResponse(
        int return_code,
        String return_message,
        int sub_return_code,
        String sub_return_message,
        String zp_trans_token,
        String order_url,
        String order_token
) {
}
