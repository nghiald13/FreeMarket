package com.ldn.inventoryservice.catalogue;

import com.ldn.common.annotations.Public;
import com.ldn.inventoryservice.catalogue.dto.request.AddProductRequest;
import com.ldn.inventoryservice.catalogue.entities.Product;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Public // Comment out for production
@RestController
@RequestMapping("/inventory/merchant/catalogue")
@RequiredArgsConstructor
public class MerchantCatalogueController {
    private final MerchantCatalogueService catalogueService;

    @GetMapping
    public Page<Product> findAll(
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize,
            @RequestHeader("X-Merchant-Id") UUID merchantId
    ) {
        return this.catalogueService.findAll(merchantId, page, pageSize);
    }

    @PostMapping("/product")
    public void addProduct(
            @RequestHeader("X-Merchant-Id") UUID merchantId,
            @RequestBody @Valid AddProductRequest addProductRequest
    ) {
        this.catalogueService.addProduct(merchantId, addProductRequest);
    }
}
