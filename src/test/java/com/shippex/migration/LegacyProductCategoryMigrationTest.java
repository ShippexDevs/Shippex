package com.shippex.migration;

import com.shippex.model.Category;
import com.shippex.model.Product;
import com.shippex.repository.CategoryRepository;
import com.shippex.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.ApplicationArguments;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LegacyProductCategoryMigrationTest {
    private final CategoryRepository categories = mock(CategoryRepository.class);
    private final ProductRepository products = mock(ProductRepository.class);
    private final MongoTemplate mongo = mock(MongoTemplate.class);
    private final ApplicationArguments args = mock(ApplicationArguments.class);

    @Test
    void dryRunReportsMatchWithoutWriting() {
        Category category = category("actual-id", "Fruits", "fruits");
        Product product = product("p-1", "Fruits", "fruits", null);
        when(categories.findAll()).thenReturn(List.of(category));
        when(products.findAll()).thenReturn(List.of(product));
        when(categories.existsById(null)).thenReturn(false);

        new LegacyProductCategoryMigration(categories, products, mongo, false, "").run(args);

        verify(mongo, never()).updateFirst(any(), any(), eq(Product.class));
    }

    @Test
    void aliasesResolveLegacyNamesAndApplyOnlyCategoryId() {
        Category category = category("actual-id", "Electronics", "electronics");
        Product product = product("p-1", "Electronic", "electronic", null);
        when(categories.findAll()).thenReturn(List.of(category));
        when(products.findAll()).thenReturn(List.of(product));
        when(categories.existsById(null)).thenReturn(false);

        new LegacyProductCategoryMigration(categories, products, mongo, true, "Electronic=Electronics").run(args);

        verify(mongo).updateFirst(any(), any(), eq(Product.class));
    }

    @Test
    void rerunCountsValidRelationshipAndDoesNotWriteAgain() {
        Category category = category("actual-id", "Fruits", "fruits");
        Product product = product("p-1", "Fruits", "fruits", "actual-id");
        when(categories.findAll()).thenReturn(List.of(category));
        when(products.findAll()).thenReturn(List.of(product));
        when(categories.existsById("actual-id")).thenReturn(true);

        new LegacyProductCategoryMigration(categories, products, mongo, true, "").run(args);

        verify(mongo, never()).updateFirst(any(), any(), eq(Product.class));
    }

    @Test
    void ambiguousAndUnmatchedMappingsAreNeverWritten() {
        Product unmatched = product("p-1", "Unknown", "unknown", null);
        Category one = category("c-1", "Produce", "produce");
        Category two = category("c-2", "Produce", "produce");
        when(categories.findAll()).thenReturn(List.of(one, two));
        when(products.findAll()).thenReturn(List.of(unmatched));
        when(categories.existsById(null)).thenReturn(false);

        new LegacyProductCategoryMigration(categories, products, mongo, true, "").run(args);

        verify(mongo, never()).updateFirst(any(), any(), eq(Product.class));
    }

    private static Category category(String id, String name, String slug) {
        Category category = new Category();
        category.setId(id); category.setName(name); category.setSlug(slug);
        return category;
    }
    private static Product product(String id, String category, String slug, String categoryId) {
        Product product = new Product();
        product.setId(id); product.setCategory(category); product.setCategorySlug(slug); product.setCategoryId(categoryId);
        return product;
    }
}
