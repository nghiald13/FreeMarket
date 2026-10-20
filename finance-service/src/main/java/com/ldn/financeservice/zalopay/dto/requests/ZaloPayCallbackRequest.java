package com.ldn.financeservice.zalopay.dto.requests;

public record ZaloPayCallbackRequest(
        String data,
        String mac,
        int type
) {
}
