package com.ldn.financeservice.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finance/wallet")
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public Long createWallet(@RequestHeader("X-Account-Id") String accountId) {
        return this.walletService.createWallet(Long.parseLong(accountId));
    }

    @GetMapping("/stripe/link")
    public void linkToStripe(@RequestHeader("X-Account-Id") Long accountId) {
        this.walletService.linkToStripe(accountId);
    }
}
