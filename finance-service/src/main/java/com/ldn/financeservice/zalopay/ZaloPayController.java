package com.ldn.financeservice.zalopay;

import com.ldn.common.annotations.Public;
import com.ldn.financeservice.zalopay.dto.requests.ZaloPayCallbackRequest;
import com.ldn.financeservice.zalopay.dto.responses.ZaloPayCallbackResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finance/zalopay")
public class ZaloPayController {
    private final ZaloPayService zaloPayService;

    @Public
    @PostMapping("/callback")
    public ZaloPayCallbackResponse callback(@RequestBody ZaloPayCallbackRequest request) {
        return this.zaloPayService.callbackHandler(request);
    }
}
