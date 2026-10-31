package com.ldn.inventoryservice.cart;

import com.ldn.inventoryservice.cart.dto.responses.CartItemResponse;
import com.ldn.inventoryservice.cart.entities.Cart;
import com.ldn.inventoryservice.cart.entities.CartItem;
import com.ldn.inventoryservice.cart.exceptions.InvalidCartItemException;
import com.ldn.inventoryservice.cart.repositories.CartItemRepository;
import com.ldn.inventoryservice.cart.repositories.CartRepository;
import com.ldn.inventoryservice.catalogue.entities.ProductVariant;
import com.ldn.inventoryservice.cart.exceptions.InvalidCartException;
import com.ldn.inventoryservice.catalogue.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository variantRepository;

    public List<CartItemResponse> getCartItemsByCustomerId(Long customerId) {
        Cart cart = this.cartRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new InvalidCartException("Customer cart not found!"));
        return this.cartItemRepository.findByCart(cart);
    }

    public void addItem(Long customerId, UUID variantId, Integer quantity) {
        Cart cart = this.cartRepository.findByCustomerId(customerId)
                .orElse(new Cart());
        ProductVariant productVariant = this.variantRepository.findById(variantId)
                .orElseThrow(InvalidCartItemException::new);

        cart.addItem(productVariant, quantity);
        this.cartRepository.save(cart);
    }

    public void removeItem(Long customerId, UUID cartItemId) {
        Cart cart = this.cartRepository.findByCustomerId(customerId)
                .orElse(null);
        if (cart == null) return;

        CartItem cartItem = this.cartItemRepository.getReferenceById(cartItemId);
        cart.removeItem(cartItem);

        this.cartRepository.save(cart);
    }

    public void updateItemQuantity(UUID cartItemId, Integer quantity) {
        CartItem cartItem = this.cartItemRepository.getReferenceById(cartItemId);
        cartItem.updateQuantity(quantity);
        this.cartItemRepository.save(cartItem);
    }
}
