package com.ldn.financeservice.wallet;

import com.ldn.financeservice.wallet.dto.requests.TopUpRequest;
import com.ldn.financeservice.wallet.dto.responses.ViewCustomerWalletResponse;
import com.ldn.financeservice.wallet.entities.Topup;
import com.ldn.financeservice.wallet.entities.Wallet;
import com.ldn.financeservice.wallet.enums.WalletStatus;
import com.ldn.financeservice.wallet.exceptions.InvalidWalletException;
import com.ldn.financeservice.wallet.repositories.TopupRepository;
import com.ldn.financeservice.zalopay.ZaloPayService;
import com.ldn.financeservice.wallet.enums.WalletType;
import com.ldn.financeservice.wallet.repositories.WalletRepository;
import jakarta.ws.rs.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final TopupRepository topupRepository;
    private final ZaloPayService zaloPayService;
    private final JobScheduler jobScheduler;
    private final WalletDelayedJob walletDelayedJob;

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

    private String getTopupLinkByMethod(Topup topup, String method) {
        switch (method.toLowerCase()) {
            case "zalopay" -> {
                String prefix = Topup.PROVIDER_REF_ID_PREFIX + topup.getWallet().getId();
                String refId = this.zaloPayService.generateAppTransId(prefix);
                topup.withZaloPay(refId);
                return this.zaloPayService.createPayUrl(
                        topup.getWallet().getId(),
                        refId,
                        "TOPUP",
                        topup.getAmount().longValue()
                );
            }
            default -> throw new BadRequestException("Invalid method!");
        }
    }

    public String topUpBalance(String idempotencyKey, Long accountId, Long walletId, TopUpRequest topUpRequest) {
        Wallet wallet = this.walletRepository.findById(walletId)
                .orElseThrow(InvalidWalletException::new);

        String method = topUpRequest.method();
        Topup topup = Topup.builder()
                .amount(topUpRequest.amount())
                .wallet(wallet)
                .build();
        String topupLink = this.getTopupLinkByMethod(topup, method);
        this.topupRepository.save(topup);

        // Schedule delayed job to handle timeout
        this.jobScheduler.schedule(
                Instant.now().plus(5, ChronoUnit.MINUTES),
                () -> walletDelayedJob.handleTopupTimeout(topup)
        );
        return topupLink;
    }
}
