package com.ldn.inventoryservice.cart;

import com.ldn.inventoryservice.cart.dto.requests.AddItemRequest;
import com.ldn.inventoryservice.cart.dto.requests.UpdateItemRequest;
import com.ldn.inventoryservice.cart.dto.responses.CartItemResponse;
import jakarta.validation.Valid;
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

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void addItem(
            @RequestHeader("X-Account-Id") Long customerId,
            @RequestBody @Valid AddItemRequest request
    ) {
        this.cartService.addItem(customerId, request.variantId(), request.quantity());
    }

    @PatchMapping
    public void updateItemQuantity(@RequestBody @Valid UpdateItemRequest request) {
        this.cartService.updateItemQuantity(request.cartItemId(), request.quantity());
    }

    @DeleteMapping
    public void removeItem(
            @RequestHeader("X-Account-Id") Long customerId,
            @RequestBody @Valid UUID cartItemId
    ) {
        this.cartService.removeItem(customerId, cartItemId);
    }
}
