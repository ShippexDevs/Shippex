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
import com.shippex.model.AppUser;
import com.shippex.security.CustomUserDetails;
import com.shippex.util.Pagination;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;

@RestController
@Validated
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(
            @PathVariable String id) {

        Product product = productService.getProductById(id);

        return ResponseEntity.ok(ProductMapper.toResponse(product));
    }

    @GetMapping("/category/{categorySlug}")
    public ResponseEntity<List<ProductResponse>> getProductsByCategorySlug(
            @PathVariable String categorySlug,
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @AuthenticationPrincipal Object principal) {

        log.info(
                "Received request to fetch products with categorySlug: {}",
                categorySlug
        );

        List<Product> products =
                productService.getProductsByCategorySlug(categorySlug);

        List<ProductResponse> response = products.stream()
                .map(ProductMapper::toResponse)
                .toList();
        return ResponseEntity.ok(pageProducts(response, offset, limit, principal));
    }

    public ResponseEntity<List<ProductResponse>> getProductsByCategorySlug(String categorySlug) {
        return getProductsByCategorySlug(categorySlug, 0, 10, null);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<ProductResponse>> getFeaturedProducts(
            @RequestParam(defaultValue = "0") @Min(0) int offset,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit,
            @AuthenticationPrincipal Object principal) {

        log.info("Received request to fetch featured products");

        List<Product> products = productService.getFeaturedProducts();

        List<ProductResponse> response = products.stream()
                .map(ProductMapper::toResponse)
                .toList();
        return ResponseEntity.ok(pageProducts(response, offset, limit, principal));
    }

    public ResponseEntity<List<ProductResponse>> getFeaturedProducts() {
        return getFeaturedProducts(0, 10, null);
    }

    private boolean appUserCanPage(Object principal) {
        return principal instanceof CustomUserDetails details && details.getUser() instanceof AppUser;
    }

    private List<ProductResponse> pageProducts(List<ProductResponse> products, int offset, int limit, Object principal) {
        if (appUserCanPage(principal)) {
            return Pagination.slice(products, offset, limit);
        }
        return Pagination.slice(products, 0, 10);
    }
}
