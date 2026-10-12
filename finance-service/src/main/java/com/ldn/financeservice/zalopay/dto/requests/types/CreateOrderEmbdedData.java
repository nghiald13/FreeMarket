package com.ldn.financeservice.zalopay.dto.requests.types;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CreateOrderEmbdedData(
        String[] preferred_payment_method,
        String redirecturl,
        String columninfo,
        String promotioninfo,
        String zlppaymentid,
        String orderType

) {
}
