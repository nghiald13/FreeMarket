package com.ldn.inventoryservice.inventory.entities;

import com.ldn.inventoryservice.inventory.enums.StockMovementType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stock_movement")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stock_item_id", nullable = false)
    private StockItem stockItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 30)
    @ToString.Include
    private StockMovementType movementType;

    @Column(name = "quantity_delta", nullable = false)
    @ToString.Include
    private int quantityDelta;

    @Column(name = "reference_type", length = 30)
    private String referenceType;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public StockMovement logInbound(StockItem stockItem, Integer quantity) {
        this.stockItem = stockItem;
        this.quantityDelta = quantity;
        this.movementType = StockMovementType.INBOUND;
        return this;
    }
}
