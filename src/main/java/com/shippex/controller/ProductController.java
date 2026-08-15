package com.shippex.controller;

import com.shippex.dto.product.CreateProductRequest;
import com.shippex.dto.product.ProductResponse;
import com.shippex.dto.product.UpdateProductRequest;
import com.shippex.mapper.ProductMapper;
import com.shippex.model.Product;
import com.shippex.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(
            @Valid @RequestBody CreateProductRequest request) {

        Product product = productService.addProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductMapper.toResponse(product));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable String id) {

        Product product = productService.getProductById(id);

        return ResponseEntity.ok(ProductMapper.toResponse(product));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable String id,
            @Valid @RequestBody UpdateProductRequest request) {

        Product product = productService.updateProductById(id, request);

        return ResponseEntity.ok(ProductMapper.toResponse(product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ProductResponse> deleteProduct(
            @PathVariable String id) {

        Product product = productService.deleteProductById(id);

        return ResponseEntity.ok(ProductMapper.toResponse(product));
    }

    @GetMapping("/category/{categorySlug}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategorySlug(
            @PathVariable String categorySlug) {

        log.info(
                "Received request to fetch products with categorySlug: {}",
                categorySlug
        );

        List<Product> products =
                productService.getProductsByCategorySlug(categorySlug);

        List<ProductResponse> response = products.stream()
                .map(ProductMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts() {

        log.info("Received request to fetch featured products");

        List<Product> products = productService.getFeaturedProducts();

        List<ProductResponse> response = products.stream()
                .map(ProductMapper::toResponse)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/bulk")
    public ResponseEntity<List<ProductResponse>> addProductsBulk(
            @RequestBody List<CreateProductRequest> products) {

        List<ProductResponse> savedProducts =
                productService.addProductsBulk(products);

        return ResponseEntity.ok(savedProducts);
    }
}