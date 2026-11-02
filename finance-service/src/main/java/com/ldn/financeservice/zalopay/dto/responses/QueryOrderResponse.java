package com.ldn.financeservice.zalopay.dto.responses;

public record QueryOrderResponse(
        Integer return_code,
        String return_message,
        Integer sub_return_code,
        String sub_return_message,
        Boolean is_processing,
        Long amount,
        Long discount_amount,
        Long zp_trans_id
) {
}
