package com.ldn.inventoryservice;

import com.ldn.common.security.Public;
import com.ldn.inventoryservice.dto.request.CartRequest;
import com.ldn.inventoryservice.dto.response.CartDetailResponse;
import com.ldn.inventoryservice.pojo.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {
    private final InventoryService inventoryService;

    @Public
    @GetMapping("/products")
    public List<Product> getAllProducts() {
        return this.inventoryService.getAllProducts();
    }

//    @PostMapping("/products")
//    @ResponseStatus(HttpStatus.CREATED)
//    public List<Product> addProducts(@RequestBody List<@Valid ProductDto> listProductDto) {
//        return this.inventoryService.addProducts(listProductDto);
//    }

    @PostMapping("/briefDetail")
    public List<CartDetailResponse> getBriefDetail(@Valid @RequestBody CartRequest cart) {
        return this.inventoryService.getBriefDetail(cart);
    }

    @GetMapping("/test")
    public String test() {
        return this.inventoryService.test();
    }
}
