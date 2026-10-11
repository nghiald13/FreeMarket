package com.ldn.financeservice.wallet;

import com.ldn.common.rabbitmq.events.AccountCreatedEvent;
import com.ldn.financeservice.rabbitmq.RabbitMQConfig;
import com.ldn.financeservice.wallet.dto.responses.ViewCustomerWalletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/finance/wallet")
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public Long activateWallet(@RequestHeader("X-Account-Id") Long accountId) {
        return this.walletService.activateWallet(accountId);
    }

    @GetMapping("/customer")
    public ViewCustomerWalletResponse viewCustomerWalletResponse(@RequestHeader("X-Account-Id") Long accountId) {
        return this.walletService.viewCustomerWallet(accountId);
    }

//    @GetMapping("/stripe/link")
//    public void linkToStripe(@RequestHeader("X-Account-Id") Long accountId) {
//        this.walletService.linkToStripe(accountId);
//    }

    // Event Listener

    @RabbitListener(queues = RabbitMQConfig.AUTH_QUEUE)
    public void onAccountCreated(AccountCreatedEvent event) {
        this.walletService.createWallet(event.accountId());
    }
}
