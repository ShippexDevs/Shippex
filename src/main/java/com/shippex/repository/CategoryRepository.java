package com.shippex.repository;

import com.shippex.model.Category;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface CategoryRepository extends MongoRepository<Category, String> {
    List<Category> findByActiveTrue();
    boolean existsByNameKeyAndIdNot(String nameKey, String id);
    boolean existsBySlugAndIdNot(String slug, String id);
    boolean existsBySkuPrefixAndIdNot(String skuPrefix, String id);
}
