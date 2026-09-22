package com.ldn.inventoryservice;

import com.ldn.inventoryservice.dto.request.CartRequest;
import com.ldn.inventoryservice.dto.response.CartDetailResponse;
import com.ldn.inventoryservice.exception.InvalidCartException;
import com.ldn.inventoryservice.pojo.Product;
import com.ldn.inventoryservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private final ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return this.productRepository.findAll();
    }

    public String test() {
        return "Test";
    }

//    @Transactional
//    public List<Product> addProducts(List<ProductDto> dto) {
//        List<Product> products = new ArrayList<>();
//        for (ProductDto p : dto) {
//            Product product = new Product();
//            product.setName(p.name());
//            product.setPrice(p.price());
//            product.setQuantity(p.quantity());
//            products.add(product);
//        }
//        return this.inventoryRepository.saveAll(products);
//    }

    public List<CartDetailResponse> getBriefDetail(CartRequest cart) {
        List<Long> productIds = cart.items().stream()
                .map(CartRequest.CartItem::productId)
                .toList();

        List<Product> products = this.productRepository.findAllById(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
        if (products.size() != cart.items().size())
             throw new InvalidCartException("Some items are missing or not existed!", HttpStatus.BAD_REQUEST);

        List<CartDetailResponse> result = cart.items().stream()
                .map(cartItem -> {
                    Product product = productMap.get(cartItem.productId());
                    return CartDetailResponse.fromEntity(product, cartItem.quantity());
                })
                .toList();

        return result;
    }
}
