package com.ldn.inventoryservice.inventory.entities;

import com.ldn.inventoryservice.inventory.enums.StockReservationStatus;
import com.ldn.inventoryservice.order.entities.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockReservation {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "quantity", nullable = false)
    @ToString.Include
    private int quantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    @ToString.Include
    private StockReservationStatus status = StockReservationStatus.ACTIVE;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public boolean isExpiredAt(Instant now) {
        return status == StockReservationStatus.ACTIVE && !expiresAt.isAfter(now);
    }

    public void commit()  { transitionFromActive(StockReservationStatus.COMMITTED); }
    public void release() { transitionFromActive(StockReservationStatus.RELEASED); }
    public void expire()  { transitionFromActive(StockReservationStatus.EXPIRED); }

    private void transitionFromActive(StockReservationStatus target) {
        if (status != StockReservationStatus.ACTIVE) {
            throw new IllegalStateException("Reservation đang ở trạng thái %s, không thể chuyển sang %s"
                    .formatted(status, target));
        }
        this.status = target;
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
}
