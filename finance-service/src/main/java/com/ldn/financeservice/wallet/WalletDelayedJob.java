package com.ldn.financeservice.wallet;

import com.ldn.financeservice.wallet.entities.Topup;
import com.ldn.financeservice.wallet.repositories.TopupRepository;
import com.ldn.financeservice.zalopay.ZaloPayService;
import lombok.RequiredArgsConstructor;
import org.jobrunr.scheduling.JobScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class WalletDelayedJob {
    private final JobScheduler jobScheduler;
    private final ZaloPayService zaloPayService;
    private final TopupRepository topupRepository;

    public void handleTopupTimeout(Topup topup) {
        switch(topup.getProvider().toLowerCase()) {
            case "zalopay" -> {
                int result = this.zaloPayService.queryOrder("TOPUP", topup.getProviderRefId());
                if (result == 2) {
                    topup.markFailed();
                    this.topupRepository.save(topup);
                } if (result == 3) {
                    jobScheduler.schedule(
                            Instant.now().plus(5, ChronoUnit.MINUTES),
                            () -> this.handleTopupTimeout(topup)
                    );
                }
            }

            default -> throw new UnsupportedOperationException();
        }
    }
}
