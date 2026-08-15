package com.shippex.service.impl;

import com.shippex.dto.product.CreateProductRequest;
import com.shippex.dto.product.ProductResponse;
import com.shippex.dto.product.UpdateProductRequest;
import com.shippex.exception.ProductNotFoundException;
import com.shippex.mapper.ProductMapper;
import com.shippex.model.Product;
import com.shippex.repository.ProductRepository;
import com.shippex.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product addProduct(CreateProductRequest request) {
        log.debug("Request entered addProduct() for: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            log.error("Product already exists with SKU: {}", request.getSku());
            throw new IllegalArgumentException(
                    "Product already exists with SKU: " + request.getSku());
        }

        Product product = new Product(
                request.getName(),
                request.getBrand(),
                request.getSku(),
                request.getDescription(),
                request.getCategory(),
                request.getCategorySlug(),
                request.getImages(),
                request.getCurrency(),
                request.getCurrentPrice(),
                request.getOriginalPrice(),
                request.getUnit(),
                request.getStock(),
                request.getDisplayOrder()
        );

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

        log.debug("Saving Product information in db for: {}", request.getSku());

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
        product.setCategory(request.getCategory());
        product.setCategorySlug(request.getCategorySlug());
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
                productRepository.findByCategorySlug(categorySlug);

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
        return products.stream()
                .filter(product -> !productRepository.existsBySku(product.getSku()))
                .map(this::createProduct)
                .toList();
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

    private ProductResponse createProduct(
            CreateProductRequest request) {

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

        Product savedProduct = productRepository.save(product);

        return ProductMapper.toResponse(savedProduct);
    }
}