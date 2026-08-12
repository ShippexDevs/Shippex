package com.shippex.repository;

import com.shippex.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends MongoRepository<Product, String> {
    boolean existsBySku(String sku);
    List<Product> findByCategorySlug(String categorySlug);
    List<Product> findByFeaturedTrueAndActiveTrue();
    Optional<Product> findBySku(String sku);
}
