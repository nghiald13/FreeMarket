package com.ldn.financeservice.wallet;

import com.ldn.financeservice.stripe.StripeService;
import com.ldn.financeservice.wallet.dto.responses.ViewCustomerWalletResponse;
import com.ldn.financeservice.wallet.entities.Wallet;
import com.ldn.financeservice.wallet.enums.WalletStatus;
import com.ldn.financeservice.wallet.exceptions.InvalidWalletException;
import com.ldn.financeservice.zalopay.ZaloPayService;
import com.ldn.financeservice.wallet.enums.WalletType;
import com.ldn.financeservice.wallet.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;

//    private final ZaloPayService zaloPayService;
//
//    private final StripeService stripeService;

    public Long createWallet(Long accountId) {
        Wallet wallet = Wallet.builder()
                .accountId(accountId)
                .walletType(WalletType.CUSTOMER)
                .build();
        this.walletRepository.save(wallet);
        return wallet.getId();
    }

    public Long activateWallet(Long accountId) {
        Wallet wallet = this.walletRepository.findByAccountIdAndWalletType(accountId, WalletType.CUSTOMER)
                .orElseThrow(InvalidWalletException::new);
        wallet.setStatus(WalletStatus.ACTIVE);
        this.walletRepository.save(wallet);
        return wallet.getId();
    }

    public ViewCustomerWalletResponse viewCustomerWallet(Long accountId) {
        Wallet wallet = this.walletRepository.findByAccountIdAndWalletType(accountId, WalletType.CUSTOMER)
                .orElseThrow(InvalidWalletException::new);
        return ViewCustomerWalletResponse.fromEntity(wallet);
    }

//    public void linkToStripe(Long accountId) {
//        this.stripeService.createLinkedAccount(accountId);
//    }
}
