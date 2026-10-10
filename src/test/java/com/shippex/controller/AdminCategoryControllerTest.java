package com.shippex.controller;

import com.shippex.dto.category.CategoryRequest;
import com.shippex.model.Category;
import com.shippex.service.CategoryService;
import jakarta.validation.Validation;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminCategoryControllerTest {
    @Test
    void bulkReportsCreatedDuplicateAndInvalidItemsIndividually() {
        CategoryService service = mock(CategoryService.class);
        var validatorFactory = Validation.buildDefaultValidatorFactory();
        try {
            AdminCategoryController controller = new AdminCategoryController(service, validatorFactory.getValidator());
            Category saved = new Category();
            saved.setId("resolved-id"); saved.setName("Fruits"); saved.setSlug("fruits"); saved.setSkuPrefix("FR");
            CategoryRequest created = new CategoryRequest("Fruits", "FR", null, null, true);
            CategoryRequest duplicate = new CategoryRequest("Duplicate", "DU", null, null, true);
            CategoryRequest invalid = new CategoryRequest(" ", "!", null, null, true);
            when(service.createCategory(created)).thenReturn(saved);
            when(service.createCategory(duplicate)).thenThrow(new IllegalArgumentException("Category name already exists"));

            var response = controller.createCategoriesBulk(List.of(created, duplicate, invalid));

            assertEquals(207, response.getStatusCode().value());
            assertEquals(1, response.getBody().getData().created());
            assertEquals(1, response.getBody().getData().skipped());
            assertEquals(1, response.getBody().getData().failed());
            assertEquals("created", response.getBody().getData().items().get(0).status());
            assertEquals("skipped", response.getBody().getData().items().get(1).status());
            assertEquals("failed", response.getBody().getData().items().get(2).status());
            assertTrue(response.getBody().getData().items().get(2).error().contains("SKU prefix"));
            verify(service, times(1)).createCategory(created);
            verify(service, times(1)).createCategory(duplicate);
            verifyNoMoreInteractions(service);
        } finally {
            validatorFactory.close();
        }
    }
}
