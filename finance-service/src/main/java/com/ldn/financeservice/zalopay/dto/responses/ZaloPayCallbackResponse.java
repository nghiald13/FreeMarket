package com.ldn.financeservice.zalopay.dto.responses;

import lombok.Builder;

@Builder
public record ZaloPayCallbackResponse(
        int return_code,
        String return_message
) {
}
