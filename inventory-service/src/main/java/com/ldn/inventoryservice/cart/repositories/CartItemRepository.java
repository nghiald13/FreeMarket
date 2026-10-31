package com.ldn.inventoryservice.cart.repositories;

import com.ldn.inventoryservice.cart.dto.responses.CartItemResponse;
import com.ldn.inventoryservice.cart.entities.Cart;
import com.ldn.inventoryservice.cart.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    @Query("""
            select ci.id as cartItemId, pv.id as variantId,
                p.name as productName, pv.name as variantName,
                pi.url as variantImage, pi.altText as variantImageAltText,
                pv.price, pv.compareAtPrice,
                ci.quantity
            from CartItem ci
            left join ProductVariant pv on ci.productVariant = pv
            left join Product p on pv.product = p
            left join ProductImage pi on ci.productVariant = pi.productVariant
            where ci.cart = :cart
            order by ci.createdAt
            """)
    List<CartItemResponse> findByCart(@Param("cart") Cart cart);
}
