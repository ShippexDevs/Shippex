package com.shippex.service;

import com.shippex.dto.category.CategoryRequest;
import com.shippex.model.Category;
import java.util.List;

public interface CategoryService {
    Category createCategory(CategoryRequest request);
    List<Category> getCategories(boolean activeOnly);
    Category getCategoryById(String id);
    Category updateCategory(String id, CategoryRequest request);
    Category updateCategoryStatus(String id, boolean active);
    String previewNextSku(String id);
    String allocateNextSku(Category category);
}
