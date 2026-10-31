package com.ldn.inventoryservice.cart;

import com.ldn.inventoryservice.cart.dto.responses.CartItemResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public List<CartItemResponse> getCustomerCartItems(
            @RequestHeader("X-Account-Id") Long customerId
    ) {
        return this.cartService.getCartItemsByCustomerId(customerId);
    }

    @PostMapping("/item/{variantId}")
    @ResponseStatus(HttpStatus.CREATED)
    public void addItem(
            @RequestHeader("X-Account-Id") Long customerId,
            @PathVariable("variantId") @Valid @NotNull UUID variantId,
            @RequestBody @Valid @NotNull Integer quantity
    ) {
        this.cartService.addItem(customerId, variantId, quantity);
    }

    @PatchMapping("/item/{cartItemId}")
    public void updateItemQuantity(
            @PathVariable("cartItemId") @Valid @NotNull  UUID cartItemId,
            @RequestBody @Valid @NotNull Integer quantity) {
        this.cartService.updateItemQuantity(cartItemId, quantity);
    }

    @DeleteMapping("/item/{cartItemId}")
    public void removeItem(
            @RequestHeader("X-Account-Id") Long customerId,
            @PathVariable("cartItemId") @Valid @NotNull UUID cartItemId
    ) {
        this.cartService.removeItem(customerId, cartItemId);
    }
}
