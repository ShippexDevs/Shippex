package com.shippex.controller;

import com.shippex.dto.ApiResponse;
import com.shippex.dto.category.CategoryRequest;
import com.shippex.dto.category.CategoryResponse;
import com.shippex.model.Category;
import com.shippex.service.CategoryService;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/admin/categories")
@RequiredArgsConstructor
@Slf4j
public class AdminCategoryController {
    private final CategoryService categoryService;
    private final Validator validator;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CategoryRequest request) {
        log.info("Admin request received to create category name={}", request.name());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(
                "Category created.", toResponse(categoryService.createCategory(request))));
    }

    @PostMapping("/bulk")
    public ResponseEntity<ApiResponse<BulkCategoryResult>> createCategoriesBulk(
            @RequestBody @Size(min = 1, max = 100, message = "Bulk request must contain 1 to 100 categories")
            List<CategoryRequest> requests) {
        List<BulkCategoryItem> items = new java.util.ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            CategoryRequest request = requests.get(i);
            if (request == null) {
                items.add(new BulkCategoryItem(i, "failed", null, "Category entry is required"));
                continue;
            }
            var violations = validator.validate(request);
            if (!violations.isEmpty()) {
                String errors = violations.stream()
                        .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                        .sorted().collect(java.util.stream.Collectors.joining("; "));
                items.add(new BulkCategoryItem(i, "failed", null, errors));
                continue;
            }
            try {
                Category created = categoryService.createCategory(request);
                items.add(new BulkCategoryItem(i, "created", toResponse(created), null));
            } catch (IllegalArgumentException ex) {
                String error = ex.getMessage() == null ? "Category is invalid" : ex.getMessage();
                String state = error.toLowerCase(java.util.Locale.ROOT).contains("already exists") ? "skipped" : "failed";
                items.add(new BulkCategoryItem(i, state, null, error));
            } catch (RuntimeException ex) {
                items.add(new BulkCategoryItem(i, "failed", null, "Category could not be created"));
            }
        }
        int created = (int) items.stream().filter(i -> i.status().equals("created")).count();
        int skipped = (int) items.stream().filter(i -> i.status().equals("skipped")).count();
        int failed = items.size() - created - skipped;
        HttpStatus status = failed == items.size() ? HttpStatus.BAD_REQUEST
                : created == 0 ? HttpStatus.OK
                : failed > 0 || skipped > 0 ? HttpStatus.MULTI_STATUS : HttpStatus.CREATED;
        return ResponseEntity.status(status).body(ApiResponse.success(
                "Bulk category import processed; existing categories were skipped without changes.",
                new BulkCategoryResult(items, created, skipped, failed)));
    }

    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAllCategories() {
        log.debug("Admin request received to fetch all categories");
        return ApiResponse.success("Categories retrieved.", categoryService.getCategories(false)
                .stream().map(AdminCategoryController::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getCategoryById(@PathVariable String id) {
        log.debug("Admin request received to fetch category id={}", id);
        return ApiResponse.success("Category retrieved.", toResponse(categoryService.getCategoryById(id)));
    }

    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable String id,
            @Valid @RequestBody CategoryRequest request) {
        log.info("Admin request received to update category id={}", id);
        return ApiResponse.success("Category updated.", toResponse(categoryService.updateCategory(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<CategoryResponse> updateCategoryStatus(@PathVariable String id,
            @RequestBody StatusRequest request) {
        log.info("Admin request received to update category status id={}, active={}", id, request.active());
        return ApiResponse.success("Category status updated.",
                toResponse(categoryService.updateCategoryStatus(id, request.active())));
    }

    @GetMapping("/{id}/next-sku")
    public ApiResponse<SkuPreview> previewNextSku(@PathVariable String id) {
        log.debug("Admin request received to preview next SKU for category id={}", id);
        Category category = categoryService.getCategoryById(id);
        return ApiResponse.success("SKU preview generated.",
                new SkuPreview(categoryService.previewNextSku(id), category.getSlug()));
    }

    private static CategoryResponse toResponse(Category category) {
        return new CategoryResponse(category.getId(), category.getName(), category.getSlug(), category.getSkuPrefix(),
                category.getDescription(), category.getImageUrl(), category.getActive(), category.getCreatedAt(),
                category.getUpdatedAt());
    }

    public record StatusRequest(boolean active) {}
    public record SkuPreview(String sku, String categorySlug) {}
    public record BulkCategoryItem(int index, String status, CategoryResponse category, String error) {}
    public record BulkCategoryResult(List<BulkCategoryItem> items, int created, int skipped, int failed) {}
}
