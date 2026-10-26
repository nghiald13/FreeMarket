package com.ldn.inventoryservice.inventory.entities;

import com.ldn.inventoryservice.catalogue.entities.ProductVariant;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name= "variant_id", nullable = false)
    private ProductVariant productVariant;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Column(name = "quantity_on_hand", nullable = false)
    @Builder.Default
    private Integer quantityOnHand = 0;

    @Column(name = "quantity_reserved", nullable = false)
    @Builder.Default
    private Integer quantityReserved = 0;

    @Column(name = "reorder_level", nullable = false)
    @Builder.Default
    private Integer reorderLevel = 0;

    public int getQuantityAvailable() {
        return quantityOnHand - quantityReserved;
    }

    public boolean isLowStock() {
        return getQuantityAvailable() <= reorderLevel;
    }

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

    /** Nhập kho. */
    public void receive(int quantity) {
        requirePositive(quantity);
        quantityOnHand += quantity;
    }

    /** Giữ hàng cho đơn (chưa trừ tồn thực). */
    public void reserve(int quantity) {
        requirePositive(quantity);
        if (quantity > getQuantityAvailable()) {
            throw new IllegalStateException("Không đủ tồn kho: cần %d, còn %d"
                    .formatted(quantity, getQuantityAvailable()));
        }
        quantityReserved += quantity;
    }

    /** Nhả hàng đã giữ (huỷ đơn / hết hạn thanh toán). */
    public void release(int quantity) {
        requirePositive(quantity);
        if (quantity > quantityReserved) {
            throw new IllegalStateException("Nhả quá số lượng đang giữ: %d > %d"
                    .formatted(quantity, quantityReserved));
        }
        quantityReserved -= quantity;
    }

    /** Chốt đơn: trừ hàng đã giữ khỏi cả reserved và on_hand. */
    public void commit(int quantity) {
        requirePositive(quantity);
        if (quantity > quantityReserved) {
            throw new IllegalStateException("Chốt quá số lượng đang giữ: %d > %d"
                    .formatted(quantity, quantityReserved));
        }
        quantityReserved -= quantity;
        quantityOnHand -= quantity;
    }

    private static void requirePositive(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải > 0");
        }
    }
}
