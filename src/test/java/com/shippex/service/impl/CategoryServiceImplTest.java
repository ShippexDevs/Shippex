package com.shippex.service.impl;

import com.shippex.dto.category.CategoryRequest;
import com.shippex.exception.CategoryNotFoundException;
import com.shippex.model.Category;
import com.shippex.model.SkuCounter;
import com.shippex.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    @Mock private CategoryRepository categories;
    @Mock private MongoTemplate mongo;
    @InjectMocks private CategoryServiceImpl categoryService;

    @BeforeEach
    void saveCategoryAsPassed() {
        lenient().when(categories.save(any(Category.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createCategoryNormalizesAndPersistsCategoryFields() {
        Category created = categoryService.createCategory(request(" Crème Brûlée ", " cb ", null));

        assertEquals("Crème Brûlée", created.getName());
        assertEquals("crème brûlée", created.getNameKey());
        assertEquals("creme-brulee", created.getSlug());
        assertEquals("CB", created.getSkuPrefix());
        assertTrue(created.getActive());
        verify(categories).save(created);
    }

    @Test
    void getCategoriesReturnsActiveOrAllCategoriesAsRequested() {
        List<Category> active = List.of(new Category());
        when(categories.findByActiveTrue()).thenReturn(active);
        List<Category> all = List.of(new Category(), new Category());
        when(categories.findAll()).thenReturn(all);

        assertSame(active, categoryService.getCategories(true));
        assertSame(all, categoryService.getCategories(false));
    }

    @Test
    void getCategoryByIdThrowsWhenCategoryDoesNotExist() {
        when(categories.findById("missing")).thenReturn(Optional.empty());
        assertThrows(CategoryNotFoundException.class, () -> categoryService.getCategoryById("missing"));
    }

    @Test
    void updateCategoryRecalculatesNormalizedFieldsAndPreservesActiveWhenOmitted() {
        Category existing = category("id-1", "Old", "old", "O", true);
        when(categories.findById("id-1")).thenReturn(Optional.of(existing));

        Category updated = categoryService.updateCategory("id-1", request("New Name", "nn", null));

        assertEquals("New Name", updated.getName());
        assertEquals("new name", updated.getNameKey());
        assertEquals("new-name", updated.getSlug());
        assertTrue(updated.getActive());
    }

    @Test
    void updateCategoryStatusPersistsRequestedStatus() {
        Category existing = category("id-1", "Fruits", "fruits", "F", true);
        when(categories.findById("id-1")).thenReturn(Optional.of(existing));

        assertFalse(categoryService.updateCategoryStatus("id-1", false).getActive());
        verify(categories).save(existing);
    }

    @Test
    void previewNextSkuUsesFirstSequenceWhenNoCounterExists() {
        when(categories.findById("id-1")).thenReturn(Optional.of(category("id-1", "Fruits", "fruits", "F", true)));
        when(mongo.findById("id-1", SkuCounter.class)).thenReturn(null);

        assertEquals("F-001", categoryService.previewNextSku("id-1"));
    }

    @Test
    void previewNextSkuIncrementsExistingSequenceAndRejectsInactiveCategory() {
        Category active = category("id-1", "Fruits", "fruits", "F", true);
        when(categories.findById("id-1")).thenReturn(Optional.of(active));
        SkuCounter counter = new SkuCounter();
        counter.setSequence(8);
        when(mongo.findById("id-1", SkuCounter.class)).thenReturn(counter);
        assertEquals("F-009", categoryService.previewNextSku("id-1"));

        active.setActive(false);
        assertThrows(IllegalArgumentException.class, () -> categoryService.previewNextSku("id-1"));
        verify(mongo, times(1)).findById("id-1", SkuCounter.class);
    }

    @Test
    void allocateNextSkuIncrementsCounterAndFormatsSku() {
        Category category = category("id-1", "Fruits", "fruits", "F", true);
        SkuCounter counter = new SkuCounter();
        counter.setSequence(12);
        when(mongo.findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
                eq(SkuCounter.class))).thenReturn(counter);

        assertEquals("F-012", categoryService.allocateNextSku(category));
        verify(mongo).findAndModify(any(Query.class), any(Update.class), any(FindAndModifyOptions.class),
                eq(SkuCounter.class));
    }

    @Test
    void createCategoryRejectsDuplicateNameSlugAndSkuPrefix() {
        when(categories.existsByNameKeyAndIdNot("fruit", null)).thenReturn(true);
        assertThrows(IllegalArgumentException.class,
                () -> categoryService.createCategory(request("Fruit", "F", true)));
    }

    @Test
    void createCategoryTranslatesDuplicateKeyFailures() {
        when(categories.save(any(Category.class))).thenThrow(new DuplicateKeyException("duplicate"));
        assertThrows(IllegalArgumentException.class,
                () -> categoryService.createCategory(request("Fruit", "F", true)));
    }

    private static CategoryRequest request(String name, String prefix, Boolean active) {
        return new CategoryRequest(name, prefix, "Description", "https://example.test/image.png", active);
    }

    private static Category category(String id, String name, String slug, String prefix, boolean active) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setSlug(slug);
        category.setSkuPrefix(prefix);
        category.setActive(active);
        return category;
    }
}
