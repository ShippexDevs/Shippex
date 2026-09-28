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
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    public ResponseEntity<ProductResponse> addProduct(
            @Valid @RequestBody CreateProductRequest request) {

        Product product = productService.addProduct(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ProductMapper.toResponse(product));
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

    @PostMapping("/bulk")
    public ResponseEntity<List<ProductResponse>> addProductsBulk(
            @RequestBody List<CreateProductRequest> products) {

        List<ProductResponse> savedProducts =
                productService.addProductsBulk(products);

        return ResponseEntity.ok(savedProducts);
    }
}
