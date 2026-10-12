package com.shippex.repository;

import com.shippex.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

public interface ProductRepository extends MongoRepository<Product, String> {
    boolean existsBySku(String sku);
    boolean existsByCategoryId(String categoryId);
    List<Product> findByCategorySlugAndActiveTrue(String categorySlug);
    List<Product> findByFeaturedTrueAndActiveTrue();
    Optional<Product> findBySku(String sku);
    @Query(value = "{ 'createdAt': { '$gte': ?0, '$lt': ?1 } }", count = true)
    long countByCreatedAtBetween(LocalDateTime from, LocalDateTime until);
    List<Product> findByCreatedAtGreaterThanEqual(LocalDateTime createdAt);
    List<Product> findByUpdatedAtGreaterThanEqual(LocalDateTime updatedAt);
}
