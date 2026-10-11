package com.ldn.financeservice.wallet.dto.responses;

import com.ldn.financeservice.wallet.entities.Wallet;
import com.ldn.financeservice.wallet.enums.WalletStatus;
import com.ldn.financeservice.wallet.enums.WalletType;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record ViewCustomerWalletResponse(
        Long walletId,
        WalletType walletType,
        String currency,
        BigDecimal balance,
        BigDecimal heldBalance,
        WalletStatus walletStatus
) {
    public static ViewCustomerWalletResponse fromEntity(Wallet wallet) {
        return new ViewCustomerWalletResponse(
                wallet.getId(),
                wallet.getWalletType(),
                wallet.getCurrency(),
                wallet.getBalance(),
                wallet.getHeldBalance(),
                wallet.getStatus()
        );
    }
}
