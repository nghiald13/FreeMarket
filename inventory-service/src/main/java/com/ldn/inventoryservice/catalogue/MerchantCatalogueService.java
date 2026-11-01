package com.ldn.inventoryservice.catalogue;

import com.ldn.inventoryservice.catalogue.dto.request.AddProductRequest;
import com.ldn.inventoryservice.catalogue.entities.Brand;
import com.ldn.inventoryservice.catalogue.entities.Product;
import com.ldn.inventoryservice.catalogue.entities.ProductImage;
import com.ldn.inventoryservice.catalogue.entities.ProductVariant;
import com.ldn.inventoryservice.catalogue.repositories.*;
import com.ldn.inventoryservice.catalogue.utils.CatalogueUtils;
import com.ldn.inventoryservice.inventory.MerchantInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MerchantCatalogueService {
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final MerchantProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final ProductImageRepository imageRepository;

    private final MerchantInventoryService inventoryService;

    public Page<Product> findAll(UUID merchantId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<Product> products = productRepository.findAllByMerchantId(merchantId, pageable);
        return products;
    }

    @Transactional
    public void addProduct(UUID merchantId, AddProductRequest request) {
        String slug = CatalogueUtils.slugify(request.name());
        Product product = Product.builder()
                .brand(brandRepository.getReferenceById(request.brandId()))
                .category(categoryRepository.getReferenceById(request.categoryId()))
                .attributes(request.attributes())
                .description(request.description())
                .merchantId(merchantId)
                .name(request.name())
                .slug(slug)
                .build();

        Map<String, Integer> skuToQuantityMap = new HashMap<>();
        request.variants().forEach(pv -> {
            ProductVariant variant = ProductVariant.builder()
                    .product(product)
                    .name(pv.name())
                    .sku(pv.sku())
                    .price(pv.price())
                    .options(pv.options())
                    .costPrice(pv.costPrice())
                    .isDefault(pv.isDefault())
                    .build();
            String altText = CatalogueUtils.slugify(String.join(" ", request.name(), pv.name()));
            ProductImage productImage = ProductImage.builder()
                    .product(product)
                    .productVariant(variant)
                    .url(pv.imageUrl())
                    .altText(altText)
                    .build();
            product.addVariant(variant);
            product.addImage(productImage);

            skuToQuantityMap.put(variant.getSku(), pv.quantityOnHand());
        });
        Product savedProduct = this.productRepository.save(product);

        Map<ProductVariant, Integer> stockItems = new HashMap<>();
        savedProduct.getVariants().forEach(v -> {
            stockItems.put(v, skuToQuantityMap.get(v.getSku()));
        });
        this.inventoryService.addStockItems(merchantId, stockItems);

    }
}
