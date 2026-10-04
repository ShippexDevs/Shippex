package com.shippex.service;

import com.shippex.dto.product.CreateProductRequest;
import com.shippex.dto.product.ProductResponse;
import com.shippex.dto.product.UpdateProductRequest;
import com.shippex.model.Product;

import java.util.List;

public interface ProductService {
    Product addProduct(CreateProductRequest product);
    List<Product> getAllProductsForAdmin();
    Product getProductById(String id);
    Product updateProductById(String id, UpdateProductRequest request);
    Product updateProductStock(String id, Integer stock);
    Product updateProductFeatured(String id, Boolean featured);
    Product updateProductActive(String id, Boolean active);
    Product deleteProductById(String id);
    List<Product> getProductsByCategorySlug(String categorySlug);
    List<Product> getFeaturedProducts();
    List<ProductResponse> addProductsBulk(
            List<CreateProductRequest> products
    );
}
