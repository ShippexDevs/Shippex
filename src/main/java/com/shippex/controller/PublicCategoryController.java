package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.category.CategoryResponse;
import com.shippex.model.Category;
import com.shippex.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class PublicCategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getActiveCategories() {
        log.debug("Public request received to fetch active categories");
        List<CategoryResponse> categoryResponses = categoryService.getCategories(true).stream()
                .map(PublicCategoryController::toResponse)
                .toList();
        return ApiResponse.success("Categories retrieved.", categoryResponses);
    }

    private static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getSkuPrefix(),
                category.getDescription(), category.getImageUrl(), category.getActive(), category.getCreatedAt(),
                category.getUpdatedAt());
    }
}
