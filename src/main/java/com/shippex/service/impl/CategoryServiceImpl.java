package com.shippex.service.impl;

import com.shippex.dto.category.CategoryRequest;
import com.shippex.model.Category;
import com.shippex.model.SkuCounter;
import com.shippex.exception.CategoryNotFoundException;
import com.shippex.repository.CategoryRepository;
import com.shippex.service.CategoryService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

@Service
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categories;
    private final MongoTemplate mongo;
    public CategoryServiceImpl(CategoryRepository categories, MongoTemplate mongo) {
        this.categories = categories; this.mongo = mongo;
    }
    @Override public Category createCategory(CategoryRequest request) {
        Category category = new Category();
        applyRequestToCategory(category, request);
        Category created = saveCategory(category);
        log.info("Created category id={}, slug={}", created.getId(), created.getSlug());
        return created;
    }
    @Override public List<Category> getCategories(boolean activeOnly) {
        return activeOnly ? categories.findByActiveTrue() : categories.findAll();
    }
    @Override public Category getCategoryById(String id) {
        return categories.findById(id).orElseThrow(() -> new CategoryNotFoundException("Category not found: " + id));
    }
    @Override public Category updateCategory(String id, CategoryRequest request) {
        Category category = getCategoryById(id);
        String oldName = category.getName();
        String oldSlug = category.getSlug();
        applyRequestToCategory(category, request);
        Category updated = saveCategory(category);
        if (!java.util.Objects.equals(oldName, updated.getName()) || !java.util.Objects.equals(oldSlug, updated.getSlug())) {
            Query products = Query.query(Criteria.where("categoryId").is(updated.getId()));
            Update denormalized = new Update().set("category", updated.getName()).set("categorySlug", updated.getSlug());
            mongo.updateMulti(products, denormalized, com.shippex.model.Product.class);
        }
        log.info("Updated category id={}, slug={}", updated.getId(), updated.getSlug());
        return updated;
    }
    @Override public Category updateCategoryStatus(String id, boolean active) {
        Category category = getCategoryById(id);
        category.setActive(active);
        Category updated = saveCategory(category);
        log.info("Updated category status id={}, active={}", id, active);
        return updated;
    }
    @Override public String previewNextSku(String id) {
        Category category = getCategoryById(id);
        if (!Boolean.TRUE.equals(category.getActive())) throw new IllegalArgumentException("Inactive category cannot be used for new products");
        SkuCounter counter = mongo.findById(id, SkuCounter.class);
        return category.getSkuPrefix() + "-" + String.format(Locale.ROOT, "%03d", counter == null ? 1 : counter.getSequence() + 1);
    }
    @Override public String allocateNextSku(Category category) {
        Query q = Query.query(Criteria.where("_id").is(category.getId()));
        Update u = new Update().inc("sequence", 1);
        SkuCounter counter = mongo.findAndModify(q, u, FindAndModifyOptions.options().upsert(true).returnNew(true), SkuCounter.class);
        String sku = category.getSkuPrefix() + "-" + String.format(Locale.ROOT, "%03d", counter.getSequence());
        log.debug("Allocated SKU for category id={}, sku={}", category.getId(), sku);
        return sku;
    }
    private void applyRequestToCategory(Category c, CategoryRequest r) {
        c.setName(r.name().trim()); c.setNameKey(normalizeKey(r.name()));
        c.setSlug(slugify(r.name())); c.setSkuPrefix(r.skuPrefix().trim().toUpperCase(Locale.ROOT));
        c.setDescription(r.description()); c.setImageUrl(r.imageUrl());
        c.setActive(r.active() == null ? (c.getActive() == null || c.getActive()) : r.active());
        if (categories.existsByNameKeyAndIdNot(c.getNameKey(), c.getId())) throw new IllegalArgumentException("Category name already exists");
        if (categories.existsBySlugAndIdNot(c.getSlug(), c.getId())) throw new IllegalArgumentException("Category slug already exists");
        if (categories.existsBySkuPrefixAndIdNot(c.getSkuPrefix(), c.getId())) throw new IllegalArgumentException("SKU prefix already exists");
    }
    private Category saveCategory(Category c) {
        try { return categories.save(c); } catch (DuplicateKeyException ex) { throw new IllegalArgumentException("Category name, slug, or SKU prefix already exists"); }
    }
    static String normalizeKey(String value) { return Normalizer.normalize(value.trim(), Normalizer.Form.NFKC).toLowerCase(Locale.ROOT); }
    static String slugify(String value) {
        String ascii = Normalizer.normalize(value.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        String slug = ascii.replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (slug.isBlank()) throw new IllegalArgumentException("Category name must contain letters or numbers"); return slug;
    }
}
