package com.ldn.financeservice.wallet;

import com.ldn.common.outbox.entities.ProcessedMessage;
import com.ldn.common.outbox.repositories.ProcessedMessageRepository;
import com.ldn.common.rabbitmq.RabbitMQEvents;
import com.ldn.common.rabbitmq.events.TopupSuccessEvent;
import com.ldn.financeservice.wallet.entities.LedgerEntry;
import com.ldn.financeservice.wallet.entities.LedgerTransaction;
import com.ldn.financeservice.wallet.entities.Topup;
import com.ldn.financeservice.wallet.entities.Wallet;
import com.ldn.financeservice.wallet.enums.LedgerTransactionType;
import com.ldn.financeservice.wallet.repositories.LedgerEntryRepository;
import com.ldn.financeservice.wallet.repositories.LedgerTransactionRepository;
import com.ldn.financeservice.wallet.repositories.TopupRepository;
import com.ldn.financeservice.wallet.repositories.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class WalletEventListener {
    private final WalletRepository walletRepository;
    private final TopupRepository topupRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final LedgerTransactionRepository ledgerTransactionRepository;
    private final ProcessedMessageRepository processedMessageRepository;

    @RabbitListener(queues = RabbitMQEvents.TOPUP_SUCCESS)
    @Transactional
    public void onTopupSuccess(
            @Header(AmqpHeaders.MESSAGE_ID) String messageId,
            TopupSuccessEvent event) {
        Topup topup = this.topupRepository.findByProviderRefId(event.providerRefId())
                .orElseThrow();

        LedgerTransaction ledgerTransaction = LedgerTransaction.builder()
                .type(LedgerTransactionType.TOPUP)
                .amount(BigDecimal.valueOf(event.amount()))
                .referenceType("TOPUP")
                .referenceId(topup.getId().toString())
                .idempotencyKey(messageId)
                .description("Nạp tiền vào ví điện tử")
                .build();
        this.ledgerTransactionRepository.save(ledgerTransaction);

        topup.markSucceeded(event.providerTxnId(), ledgerTransaction);
        this.topupRepository.save(topup);

        Wallet walletSC = this.walletRepository.getSystemClearingWallet();
        LedgerEntry debit = new LedgerEntry().debit(walletSC, ledgerTransaction);
        walletSC.debit(ledgerTransaction.getAmount());

        Wallet walletTU = topup.getWallet();
        LedgerEntry credit = new LedgerEntry().credit(walletTU, ledgerTransaction);
        walletTU.credit(ledgerTransaction.getAmount());

        List<LedgerEntry> entries = List.of(debit, credit);

        this.ledgerEntryRepository.saveAll(entries);

        ProcessedMessage processedMessage = ProcessedMessage.builder()
                .messageId(messageId)
                .consumer(WalletEventListener.class.getName())
                .build();
        this.processedMessageRepository.save(processedMessage);
    }
}
