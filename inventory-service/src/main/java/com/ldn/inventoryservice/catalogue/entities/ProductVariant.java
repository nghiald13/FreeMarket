package com.ldn.inventoryservice.catalogue.entities;

import com.ldn.inventoryservice.catalogue.enums.ProductStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "product_variant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariant {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "sku", nullable = false, length = 100, unique = true)
    private String sku;

    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "name", length = 500)
    private String name;

    @Column(name = "price", nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(name = "compare_at_price", precision = 19, scale = 2)
    private BigDecimal compareAtPrice;

    @Column(name = "cost_price", precision = 19, scale = 2)
    private BigDecimal costPrice;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "weight_grams")
    private Integer weightGrams;

    /** Ví dụ {"color":"red","size":"M"}. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "options", nullable = false)
    @Builder.Default
    private Map<String, Object> options = new HashMap<>();

    @Column(name = "is_default", nullable = false)
    @Builder.Default
    private boolean isDefault = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean isActive = true;

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
