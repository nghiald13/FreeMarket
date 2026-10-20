package com.ldn.financeservice.zalopay;

import com.ldn.common.outbox.OutboxService;
import com.ldn.common.rabbitmq.DomainEvent;
import com.ldn.common.rabbitmq.events.PaymentSuccessEvent;
import com.ldn.common.rabbitmq.events.TopupSuccessEvent;
import com.ldn.common.redis.RedisService;
import com.ldn.financeservice.zalopay.configs.ZaloPayProperties;
import com.ldn.financeservice.zalopay.dto.requests.QueryOrderRequest;
import com.ldn.financeservice.zalopay.dto.requests.types.CreateOrderEmbdedData;
import com.ldn.financeservice.zalopay.dto.requests.types.CreateOrderItem;
import com.ldn.financeservice.zalopay.dto.requests.CreateOrderRequest;
import com.ldn.financeservice.zalopay.dto.requests.ZaloPayCallbackRequest;
import com.ldn.financeservice.zalopay.dto.requests.types.ZaloPayCallbackData;
import com.ldn.financeservice.zalopay.dto.responses.CreateOrderResponse;
import com.ldn.financeservice.zalopay.dto.responses.QueryOrderResponse;
import com.ldn.financeservice.zalopay.dto.responses.ZaloPayCallbackResponse;
import com.ldn.financeservice.zalopay.enums.ZaloPayAPI;
import com.ldn.financeservice.zalopay.exceptions.InvalidCallbackSignatureException;
import com.ldn.financeservice.zalopay.exceptions.OrderHandledException;
import com.ldn.financeservice.zalopay.utils.ZaloPayHMACUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ZaloPayService {
    private final ZaloPayProperties zaloPayProperties;
    private final RestClient zaloPayClient;
    private final ObjectMapper objectMapper;
    private final OutboxService outboxService;
    private final RedisService redisService;

    public String createPayUrlPayload(Long accountId, Long orderId, Long amount) {
        String appTransId = "%s_%d".formatted(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")),
                orderId
        );
        Long appTime = Instant.now().toEpochMilli();

        CreateOrderEmbdedData createOrderEmbdedData = CreateOrderEmbdedData.builder()
                .redirecturl("http://localhost:3000")
                .build();
        String embdedData = objectMapper.writeValueAsString(createOrderEmbdedData);

        List<CreateOrderItem> createOrderItem = new ArrayList<>();
        String item = objectMapper.writeValueAsString(createOrderItem);

        String mac = ZaloPayHMACUtils.HMacHexStringEncode(ZaloPayHMACUtils.HMACSHA256, zaloPayProperties.key1(),
                "%s|%s|%s|%s|%s|%s|%s".formatted(
                        zaloPayProperties.appId(),
                        appTransId,
                        accountId,
                        amount,
                        appTime,
                        embdedData,
                        item
                )
        );
        CreateOrderRequest requestBody = CreateOrderRequest.builder()
                .app_id(zaloPayProperties.appId())
                .app_user(accountId.toString())
                .app_trans_id(appTransId)
                .app_time(appTime)
                .expire_duration_seconds(300L)
                .amount(amount)
                .item(item)
                .description("Thanh toán cho đơn hàng")
                .embed_data(embdedData)
                .mac(mac)
                .callback_url(zaloPayProperties.webhook())
                .build();

        return objectMapper.writeValueAsString(requestBody);
    }

    public String getPayUrlResponse(String payload) {
        CreateOrderResponse response = this.zaloPayClient.post()
                .uri(ZaloPayAPI.CREATE_ORDER)
                .contentType(MediaType.APPLICATION_JSON)
                .body(payload)
                .retrieve()
                .body(CreateOrderResponse.class);

        return objectMapper.writeValueAsString(response);
    }

    /**
     *
     * @param payload (response from create order)
     * @return
     */
    public String retrievePayUrl(String payload) {
        CreateOrderResponse response = objectMapper.readValue(payload, CreateOrderResponse.class);
        return response.order_url();
    }

    /**
     *
     * @param payload (response from create order)
     * @return
     */
    public String retrieveTxnId(String payload) {
        CreateOrderResponse response = objectMapper.readValue(payload, CreateOrderResponse.class);
        return response.zp_trans_token();
    }

    /**
     * Generate id with ZaloPay required format for query
     *
     * @param orderId
     * @return
     */
    public String generateAppTransId(String orderId) {
        return "%s_%s".formatted(
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss")),
                orderId
        );
    }

    /**
     *
     * @param accountId  or walletId
     * @param appTransId generateAppTransId()
     * @param orderType  "PAYMENT", "TOPUP"
     * @param amount
     * @return
     */
    public String createPayUrl(Long accountId, String appTransId, String orderType, Long amount) {
        Long appTime = Instant.now().toEpochMilli();

        CreateOrderEmbdedData createOrderEmbdedData = CreateOrderEmbdedData.builder()
                .redirecturl("http://localhost:3000")
                .orderType(orderType)
                .build();
        String embdedData = objectMapper.writeValueAsString(createOrderEmbdedData);

        List<CreateOrderItem> createOrderItem = new ArrayList<>();
        String item = objectMapper.writeValueAsString(createOrderItem);

        String mac = ZaloPayHMACUtils.HMacHexStringEncode(ZaloPayHMACUtils.HMACSHA256, zaloPayProperties.key1(),
                "%s|%s|%s|%s|%s|%s|%s".formatted(
                        zaloPayProperties.appId(),
                        appTransId,
                        accountId,
                        amount,
                        appTime,
                        embdedData,
                        item
                )
        );
        CreateOrderRequest requestBody = CreateOrderRequest.builder()
                .app_id(zaloPayProperties.appId())
                .app_user(accountId.toString())
                .app_trans_id(appTransId)
                .app_time(appTime)
                .expire_duration_seconds(300L)
                .amount(amount)
                .item(item)
                .description("Thanh toán cho đơn hàng")
                .embed_data(embdedData)
                .mac(mac)
                .callback_url(zaloPayProperties.webhook())
                .build();

        CreateOrderResponse response = this.zaloPayClient.post()
                .uri(ZaloPayAPI.CREATE_ORDER)
                .body(requestBody)
                .retrieve()
                .body(CreateOrderResponse.class);

        return response.order_url();
    }

    @Transactional
    public ZaloPayCallbackResponse callbackHandler(ZaloPayCallbackRequest request) {
        // Verify Mac
        String verifyMac = ZaloPayHMACUtils.HMacHexStringEncode(ZaloPayHMACUtils.HMACSHA256, zaloPayProperties.key2(), request.data());
        if (!verifyMac.equals(request.mac())) throw new InvalidCallbackSignatureException();

        // Parse data
        ZaloPayCallbackData data = objectMapper.readValue(request.data(), ZaloPayCallbackData.class);
        CreateOrderEmbdedData embdedData = objectMapper.readValue(data.embed_data(), CreateOrderEmbdedData.class);

        // Idempotency
        // Guard 1: Redis distributed lock
        String cacheKey = "%s:%s".formatted(embdedData.orderType(), data.app_trans_id());
        Long cacheValue = data.zp_trans_id();
        boolean cacheable = this.redisService.setIfAbsent(cacheKey, cacheValue, 1, TimeUnit.DAYS);
        if (!cacheable) throw new OrderHandledException();
        // Guard 2: Database
        boolean recorded = this.outboxService.existsByAggregateId(data.app_trans_id());
        if (recorded) throw new OrderHandledException();

        // Announce event
        this.announceEvent(embdedData.orderType(), data.amount(), data.zp_trans_id(), data.app_trans_id());

        // Reponse to ZaloPay Api
        return ZaloPayCallbackResponse.builder()
                .return_code(1)
                .return_message("Đã xử lý giao dịch!")
                .build();
    }

    public void announceEvent(String orderType, Long amount, Long providerTxnId, String providerRefId) {
        // Event coordination
        DomainEvent event;
        if (orderType.equals("PAYMENT"))
            event = PaymentSuccessEvent.builder()
                    .build();
        else if (orderType.equals("TOPUP"))
            event = TopupSuccessEvent.builder()
                    .amount(amount)
                    .providerRefId(providerRefId)
                    .providerTxnId(providerTxnId.toString())
                    .build();
        else throw new InvalidCallbackSignatureException();
        this.outboxService.record(
                orderType,
                providerRefId,
                event
        );
    }

    /**
     *
     * @param orderType
     * @param providerRefId
     * @return 1 = success, 2 = failed, 3 = processing
     * Handle case 2 and 3 only, case 1 act like webhook
     */
    public int queryOrder(String orderType, String providerRefId) {
        int appId = this.zaloPayProperties.appId();
        String mac = ZaloPayHMACUtils.HMacHexStringEncode(ZaloPayHMACUtils.HMACSHA256, this.zaloPayProperties.key1(), providerRefId);
        QueryOrderRequest request = QueryOrderRequest.builder()
                .app_id(appId)
                .app_trans_id(providerRefId)
                .mac(mac)
                .build();

        QueryOrderResponse response = this.zaloPayClient.post()
                .uri(ZaloPayAPI.QUERY_ORDER)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(QueryOrderResponse.class);

        if (response.return_code() == 1)
            this.announceEvent(orderType, response.amount(), response.zp_trans_id(), providerRefId);

        return response.return_code();

    }
}