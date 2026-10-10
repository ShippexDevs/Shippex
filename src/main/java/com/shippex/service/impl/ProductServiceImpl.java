package com.shippex.service.impl;

import com.shippex.dto.product.CreateProductRequest;
import com.shippex.dto.product.ProductResponse;
import com.shippex.dto.product.UpdateProductRequest;
import com.shippex.exception.ProductNotFoundException;
import com.shippex.exception.CategoryNotFoundException;
import com.shippex.mapper.ProductMapper;
import com.shippex.model.Product;
import com.shippex.repository.ProductRepository;
import com.shippex.repository.CategoryRepository;
import com.shippex.model.Category;
import com.shippex.service.ProductService;
import com.shippex.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;

    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.categoryService = categoryService;
    }

    @Override
    public List<Product> getAllProductsForAdmin() {
        return productRepository.findAll();
    }

    @Override
    public Product addProduct(CreateProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + request.getCategoryId()));
        if (!Boolean.TRUE.equals(category.getActive())) throw new IllegalArgumentException("Inactive category cannot be used for new products");
        String generatedSku = categoryService.allocateNextSku(category);

        Product product = new Product(
                request.getName(),
                request.getBrand(),
                generatedSku,
                request.getDescription(),
                category.getName(),
                category.getSlug(),
                request.getImages(),
                request.getCurrency(),
                request.getCurrentPrice(),
                request.getOriginalPrice(),
                request.getUnit(),
                request.getStock(),
                request.getDisplayOrder()
        );

        product.setCategoryId(category.getId());
        product.setCategory(category.getName());
        product.setCategorySlug(category.getSlug());

        product.setTags(request.getTags());

        if (request.getFeatured() != null) {
            product.setFeatured(request.getFeatured());
        }

        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        if (request.getDeliveryTime() != null) {
            product.setDeliveryTime(request.getDeliveryTime());
        }

        log.debug("Saving Product information in db for category {}", category.getId());

        return productRepository.save(product);
    }

    @Override
    public Product getProductById(String id) {
        log.debug("Fetching product with id: {}", id);
        return findProductById(id);
    }

    @Override
    public Product updateProductById(String id, UpdateProductRequest request) {

        log.info("Updating product {}", id);

        Product product = findProductById(id);

        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        if (request.getCategoryId() != null && !request.getCategoryId().isBlank()) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found: " + request.getCategoryId()));
            if (!Boolean.TRUE.equals(category.getActive())) {
                throw new IllegalArgumentException("Inactive category cannot be assigned to a product");
            }
            product.setCategoryId(category.getId());
            product.setCategory(category.getName());
            product.setCategorySlug(category.getSlug());
        }
        product.setImages(request.getImages());
        product.setCurrency(request.getCurrency());
        product.setCurrentPrice(request.getCurrentPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setUnit(request.getUnit());
        product.setStock(request.getStock());
        product.setTags(request.getTags());
        product.setFeatured(request.getFeatured());
        product.setActive(request.getActive());
        product.setDeliveryTime(request.getDeliveryTime());
        product.setDisplayOrder(request.getDisplayOrder());

        Product updated = productRepository.save(product);

        log.info("Product {} updated successfully", id);

        return updated;
    }

    @Override
    public Product updateProductStock(String id, Integer stock) {
        Product product = findProductById(id);

        product.setStock(stock);
        return productRepository.save(product);
    }

    @Override
    public Product updateProductFeatured(String id, Boolean featured) {
        Product product = findProductById(id);
        product.setFeatured(featured);
        return productRepository.save(product);
    }

    @Override
    public Product updateProductActive(String id, Boolean active) {
        Product product = findProductById(id);
        product.setActive(active);
        return productRepository.save(product);
    }

    @Override
    public Product deleteProductById(String id) {

        log.debug("Deleting product with id: {}", id);

        Product product = findProductById(id);

        productRepository.delete(product);

        log.debug("Product deleted successfully: {}", id);

        return product;
    }

    @Override
    public List<Product> getProductsByCategorySlug(String categorySlug) {
        log.debug("Fetching products with categorySlug: {}", categorySlug);

        List<Product> products =
                productRepository.findByCategorySlugAndActiveTrue(categorySlug);

        log.debug(
                "Found {} products with categorySlug: {}",
                products.size(),
                categorySlug
        );

        return products;
    }

    @Override
    public List<Product> getFeaturedProducts() {

        log.debug("Fetching active featured products");

        List<Product> products = productRepository.findByFeaturedTrueAndActiveTrue();

        log.debug("Found {} featured products", products.size());

        return products;
    }

    @Override
    public List<ProductResponse> addProductsBulk(List<CreateProductRequest> products) {
        return products.stream().map(this::addProduct).map(ProductMapper::toResponse).toList();
    }

    /**
     * Helper method to fetch a product or throw exception.
     */
    private Product findProductById(String id) {
        return productRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Product not found with id: {}", id);
                    return new ProductNotFoundException(
                            "Product not found with id: " + id);
                });
    }

    private ProductResponse createProduct(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setBrand(request.getBrand());
        product.setSku(request.getSku());
        product.setDescription(request.getDescription());
        product.setCategory(request.getCategory());
        product.setCategorySlug(request.getCategorySlug());
        product.setImages(request.getImages());
        product.setCurrency(request.getCurrency());
        product.setCurrentPrice(request.getCurrentPrice());
        product.setOriginalPrice(request.getOriginalPrice());
        product.setUnit(request.getUnit());
        product.setStock(request.getStock());
        product.setFeatured(request.getFeatured());
        product.setActive(request.getActive());
        product.setDeliveryTime(request.getDeliveryTime());
        product.setTags(request.getTags());
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return ProductMapper.toResponse(productRepository.save(product));
    }

}
