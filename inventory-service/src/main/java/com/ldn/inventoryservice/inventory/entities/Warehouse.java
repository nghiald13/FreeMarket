package com.ldn.inventoryservice.inventory.entities;

import com.ldn.inventoryservice.inventory.enums.WarehouseOwner;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "warehouse")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "merchant_id")
    private UUID merchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "owner_type", nullable = false)
    @Builder.Default
    private WarehouseOwner warehouseOwner = WarehouseOwner.MERCHANT;

    @Column(name = "code", nullable = false, unique = true)
    private String code;

    @Column(name = "name", nullable = false)
    private String name;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "address")
    private String address;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

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

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "warehouse")
    public List<StockItem> stockItems = new ArrayList<>();

    public Warehouse createForMerchant(UUID merchantId, String code, String name, String address) {
        this.code = code;
        this.name = name;
        this.address = address;
        this.merchantId = merchantId;
        this.warehouseOwner = WarehouseOwner.MERCHANT;
        return this;
    }

    public void addStockItem(StockItem stockItem) {
        this.stockItems.add(stockItem);
    }
}
