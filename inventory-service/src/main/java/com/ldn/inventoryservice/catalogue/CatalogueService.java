package com.ldn.inventoryservice.catalogue;

import com.ldn.inventoryservice.cart.repositories.CartRepository;
import com.ldn.inventoryservice.catalogue.dto.response.ProductDetailResponse;
import com.ldn.inventoryservice.catalogue.dto.response.types.ProductDetail;
import com.ldn.inventoryservice.catalogue.entities.Product;
import com.ldn.inventoryservice.catalogue.entities.ProductVariant;
import com.ldn.inventoryservice.catalogue.repositories.ProductRepository;
import com.ldn.inventoryservice.catalogue.repositories.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CatalogueService {
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CartRepository cartRepository;

    /**
     * Get product details by ID
     *
     * @param id List of productId requires details
     * @return a
     */
    public ProductDetailResponse getProductDetails(List<UUID> id) {
        List<ProductVariant> products = this.productVariantRepository.findAllById(id);
        List<ProductDetail> productDetails = new ArrayList<>();
        products.forEach(product -> {
            productDetails.add(ProductDetail.builder()
                    .productId(product.getId())
                    .productName(product.getProduct().getName())
                    .productVariant(product.getName())
                    .productPrice(product.getPrice())
                    .build());
        });

        return ProductDetailResponse.fromList(productDetails);
    }

    public Slice<Product> findHotX(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(Sort.Direction.DESC, "createdAt"));

        return this.productRepository.findAll(pageable);
    }

    public Slice<Product> findByMerchantId(Long merchantId, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by("createdAt").descending());
        return this.productRepository.findByMerchantId(merchantId, pageable);
    }


}
