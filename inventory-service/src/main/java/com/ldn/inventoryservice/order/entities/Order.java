package com.ldn.inventoryservice.order.entities;

import com.ldn.inventoryservice.order.enums.OrderStatus;
import com.ldn.inventoryservice.order.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.generator.EventType;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /** Do DB sinh từ order_number_seq; Hibernate đọc lại sau khi INSERT. */
    @org.hibernate.annotations.Generated(event = EventType.INSERT)
    @Column(name = "order_number", nullable = false, updatable = false)
    @Setter(AccessLevel.NONE)
    private Long orderNumber;

    /** user-service, không FK. */
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "merchant_id", nullable = false)
    private UUID merchantId;

    @Column(name = "checkout_id", nullable = false)
    private UUID checkoutId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "subtotal_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal subtotalAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "shipping_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal shippingAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    /** Snapshot địa chỉ giao hàng tại thời điểm đặt. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "shipping_address", nullable = false)
    private Map<String, Object> shippingAddress;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "billing_address")
    private Map<String, Object> billingAddress;

    @Column(name = "note", length = 1000)
    private String note;

    /** Chống tạo đơn trùng khi client retry (unique theo customer_id + key). */
    @Column(name = "idempotency_key", length = 100)
    private String idempotencyKey;

    @Column(name = "placed_at")
    private Instant placedAt;

    @Column(name = "cancelled_at")
    private Instant cancelledAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    @Builder.Default
    private List<OrderStatusHistory> statusHistory = new ArrayList<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    /** total = subtotal - discount + shipping + tax; subtotal = tổng line_total. */
    public void recalculateTotals() {
        this.subtotalAmount = items.stream()
                .map(OrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalAmount = subtotalAmount
                .subtract(discountAmount)
                .add(shippingAmount)
                .add(taxAmount);
    }

    /**
     * Đổi trạng thái và ghi lịch sử. Chưa kiểm tra state machine,
     * thêm rule chuyển trạng thái hợp lệ ở đây khi nghiệp vụ rõ hơn.
     */
    public void changeStatus(OrderStatus target, String reason, UUID changedBy) {
        OrderStatus previous = this.status;
        this.status = target;
        if (target == OrderStatus.CANCELLED) {
            this.cancelledAt = Instant.now();
        }
        statusHistory.add(OrderStatusHistory.builder()
                .order(this)
                .fromStatus(previous)
                .toStatus(target)
                .reason(reason)
                .changedBy(changedBy)
                .build());
    }


}
