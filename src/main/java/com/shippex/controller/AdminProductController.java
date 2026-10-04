package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.product.CreateProductRequest;
import com.shippex.dto.product.ProductResponse;
import com.shippex.dto.product.UpdateProductRequest;
import com.shippex.dto.product.UpdateStockRequest;
import com.shippex.dto.product.UpdateFeaturedRequest;
import com.shippex.dto.product.UpdateActiveRequest;
import com.shippex.mapper.ProductMapper;
import com.shippex.model.Product;
import com.shippex.service.ProductService;
import com.shippex.util.Pagination;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@Slf4j
public class AdminProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {

        List<ProductResponse> products = productService.getAllProductsForAdmin()
                .stream()
                .map(ProductMapper::toResponse)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(
                "Products retrieved successfully.",
                Pagination.slice(products, offset, limit)
        ));
    }

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

    @PatchMapping("/{id}/stock")
    public ResponseEntity<ProductResponse> updateProductStock(
            @PathVariable String id,
            @Valid @RequestBody UpdateStockRequest request) {
        return ResponseEntity.ok(ProductMapper.toResponse(
                productService.updateProductStock(id, request.stock())));
    }

    @PatchMapping("/{id}/featured")
    public ResponseEntity<ProductResponse> updateProductFeatured(
            @PathVariable String id,
            @Valid @RequestBody UpdateFeaturedRequest request) {
        return ResponseEntity.ok(ProductMapper.toResponse(
                productService.updateProductFeatured(id, request.featured())));
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<ProductResponse> updateProductActive(
            @PathVariable String id,
            @Valid @RequestBody UpdateActiveRequest request) {
        return ResponseEntity.ok(ProductMapper.toResponse(
                productService.updateProductActive(id, request.active())));
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
