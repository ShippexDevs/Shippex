package com.shippex.service.impl;

import com.shippex.dto.product.CreateProductRequest;
import com.shippex.dto.product.ProductResponse;
import com.shippex.dto.product.UpdateProductRequest;
import com.shippex.exception.ProductNotFoundException;
import com.shippex.model.Product;
import com.shippex.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private CreateProductRequest createRequest;
    private UpdateProductRequest updateRequest;

    @BeforeEach
    void setUp() {

        product = new Product(
                "Apple",
                "Fresh Farm",
                "SKU001",
                "Fresh Apples",
                "Fruits",
                "fruits",
                List.of("img1"),
                "USD",
                new BigDecimal("10.00"),
                new BigDecimal("12.00"),
                "1 kg",
                20,
                1
        );

        product.setId("1");
        product.setActive(true);
        product.setFeatured(true);

        createRequest = new CreateProductRequest();
        createRequest.setName("Apple");
        createRequest.setBrand("Fresh Farm");
        createRequest.setSku("SKU001");
        createRequest.setDescription("Fresh Apples");
        createRequest.setCategory("Fruits");
        createRequest.setCategorySlug("fruits");
        createRequest.setImages(List.of("img1"));
        createRequest.setCurrency("USD");
        createRequest.setCurrentPrice(new BigDecimal("10.00"));
        createRequest.setOriginalPrice(new BigDecimal("12.00"));
        createRequest.setUnit("1 kg");
        createRequest.setStock(20);
        createRequest.setDisplayOrder(1);

        updateRequest = new UpdateProductRequest();
        updateRequest.setName("Green Apple");
        updateRequest.setBrand("Fresh Farm");
        updateRequest.setDescription("Updated");
        updateRequest.setCategory("Fruits");
        updateRequest.setCategorySlug("fruits");
        updateRequest.setImages(List.of("img2"));
        updateRequest.setCurrency("USD");
        updateRequest.setCurrentPrice(new BigDecimal("15.00"));
        updateRequest.setOriginalPrice(new BigDecimal("18.00"));
        updateRequest.setUnit("1 kg");
        updateRequest.setStock(30);
        updateRequest.setTags(List.of("organic"));
        updateRequest.setFeatured(true);
        updateRequest.setActive(true);
        updateRequest.setDeliveryTime("30 mins");
        updateRequest.setDisplayOrder(2);
    }

    @Test
    void addProduct_ShouldSaveProduct() {

        when(productRepository.existsBySku("SKU001")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product saved = productService.addProduct(createRequest);

        assertNotNull(saved);
        assertEquals("SKU001", saved.getSku());

        verify(productRepository).existsBySku("SKU001");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void addProduct_ShouldThrowException_WhenSkuAlreadyExists() {

        when(productRepository.existsBySku("SKU001")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> productService.addProduct(createRequest));

        assertEquals("Product already exists with SKU: SKU001", ex.getMessage());

        verify(productRepository).existsBySku("SKU001");
        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_ShouldReturnProduct() {

        when(productRepository.findById("1"))
                .thenReturn(Optional.of(product));

        Product result = productService.getProductById("1");

        assertNotNull(result);
        assertEquals("1", result.getId());

        verify(productRepository).findById("1");
    }

    @Test
    void getProductById_ShouldThrowException_WhenNotFound() {

        when(productRepository.findById("1"))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductById("1"));

        verify(productRepository).findById("1");
    }

    @Test
    void updateProduct_ShouldUpdateFields() {

        when(productRepository.findById("1"))
                .thenReturn(Optional.of(product));

        when(productRepository.save(any(Product.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Product updated = productService.updateProductById("1", updateRequest);

        assertEquals("Green Apple", updated.getName());
        assertEquals(new BigDecimal("15.00"), updated.getCurrentPrice());
        assertEquals(30, updated.getStock());

        verify(productRepository).findById("1");
        verify(productRepository).save(product);
    }

    @Test
    void updateProduct_ShouldThrowException_WhenProductNotFound() {

        when(productRepository.findById("1"))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProductById("1", updateRequest));

        verify(productRepository).findById("1");
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProduct_ShouldDeleteProduct() {

        when(productRepository.findById("1"))
                .thenReturn(Optional.of(product));

        Product deleted = productService.deleteProductById("1");

        assertNotNull(deleted);
        assertEquals("1", deleted.getId());

        verify(productRepository).findById("1");
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_ShouldThrowException_WhenProductNotFound() {

        when(productRepository.findById("1"))
                .thenReturn(Optional.empty());

        assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProductById("1"));

        verify(productRepository).findById("1");
        verify(productRepository, never()).delete(any(Product.class));
    }

    @Test
    void getProductsByCategorySlug_ShouldReturnProducts() {

        String categorySlug = "fruits";

        Product product1 = new Product(
                "Apple",
                "Fresh Farm",
                "SKU001",
                "Fresh Apples",
                "Fruits",
                categorySlug,
                List.of("apple.jpg"),
                "USD",
                new BigDecimal("10.00"),
                new BigDecimal("12.00"),
                "1 kg",
                20,
                1
        );

        product1.setId("1");

        Product product2 = new Product(
                "Banana",
                "Fresh Farm",
                "SKU002",
                "Fresh Bananas",
                "Fruits",
                categorySlug,
                List.of("banana.jpg"),
                "USD",
                new BigDecimal("5.00"),
                new BigDecimal("6.00"),
                "1 kg",
                30,
                2
        );

        product2.setId("2");

        when(productRepository.findByCategorySlug(categorySlug))
                .thenReturn(List.of(product1, product2));

        List<Product> result =
                productService.getProductsByCategorySlug(categorySlug);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("1", result.get(0).getId());
        assertEquals("Apple", result.get(0).getName());
        assertEquals(categorySlug, result.get(0).getCategorySlug());

        assertEquals("2", result.get(1).getId());
        assertEquals("Banana", result.get(1).getName());
        assertEquals(categorySlug, result.get(1).getCategorySlug());

        verify(productRepository).findByCategorySlug(categorySlug);
    }

    @Test
    void getProductsByCategorySlug_ShouldReturnEmptyList_WhenNoProductsFound() {

        String categorySlug = "non-existing-category";

        when(productRepository.findByCategorySlug(categorySlug))
                .thenReturn(Collections.emptyList());

        List<Product> result =
                productService.getProductsByCategorySlug(categorySlug);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository).findByCategorySlug(categorySlug);
    }

    @Test
    void getFeaturedProducts_ShouldReturnFeaturedProducts() {

        Product product1 = new Product(
                "Apple",
                "Fresh Farm",
                "SKU001",
                "Fresh Apples",
                "Fruits",
                "fruits",
                List.of("apple.jpg"),
                "USD",
                new BigDecimal("10.00"),
                new BigDecimal("12.00"),
                "1 kg",
                20,
                1
        );

        product1.setId("1");
        product1.setFeatured(true);

        Product product2 = new Product(
                "Banana",
                "Fresh Farm",
                "SKU002",
                "Fresh Bananas",
                "Fruits",
                "fruits",
                List.of("banana.jpg"),
                "USD",
                new BigDecimal("5.00"),
                new BigDecimal("6.00"),
                "1 kg",
                30,
                2
        );

        product2.setId("2");
        product2.setFeatured(true);

        when(productRepository.findByFeaturedTrueAndActiveTrue())
                .thenReturn(List.of(product1, product2));

        List<Product> result = productService.getFeaturedProducts();

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("1", result.get(0).getId());
        assertEquals("Apple", result.get(0).getName());
        assertTrue(result.get(0).getFeatured());

        assertEquals("2", result.get(1).getId());
        assertEquals("Banana", result.get(1).getName());
        assertTrue(result.get(1).getFeatured());

        verify(productRepository).findByFeaturedTrueAndActiveTrue();
    }

    @Test
    void getFeaturedProducts_ShouldReturnEmptyList_WhenNoFeaturedProductsFound() {

        when(productRepository.findByFeaturedTrueAndActiveTrue())
                .thenReturn(List.of());

        List<Product> result = productService.getFeaturedProducts();

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository).findByFeaturedTrueAndActiveTrue();
    }

    @Test
    void addProductsBulk_ShouldSaveProduct_WhenSkuDoesNotExist() {

        when(productRepository.existsBySku("SKU001"))
                .thenReturn(false);

        when(productRepository.save(any(Product.class)))
                .thenReturn(product);

        List<ProductResponse> result =
                productService.addProductsBulk(List.of(createRequest));

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals("SKU001", result.get(0).getSku());
        assertEquals("Apple", result.get(0).getName());

        verify(productRepository).existsBySku("SKU001");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void addProductsBulk_ShouldSkipProduct_WhenSkuAlreadyExists() {

        when(productRepository.existsBySku("SKU001"))
                .thenReturn(true);

        List<ProductResponse> result =
                productService.addProductsBulk(List.of(createRequest));

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(productRepository).existsBySku("SKU001");

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void addProductsBulk_ShouldInsertOnlyNewProducts() {

        CreateProductRequest existingRequest = new CreateProductRequest();
        existingRequest.setName("Existing Apple");
        existingRequest.setBrand("Fresh Farm");
        existingRequest.setSku("SKU001");

        CreateProductRequest newRequest = new CreateProductRequest();
        newRequest.setName("Banana");
        newRequest.setBrand("Fresh Farm");
        newRequest.setSku("SKU002");
        newRequest.setDescription("Fresh Bananas");
        newRequest.setCategory("Fruits");
        newRequest.setCategorySlug("fruits");
        newRequest.setImages(List.of("banana.jpg"));
        newRequest.setCurrency("USD");
        newRequest.setCurrentPrice(new BigDecimal("5.00"));
        newRequest.setOriginalPrice(new BigDecimal("6.00"));
        newRequest.setUnit("1 kg");
        newRequest.setStock(30);
        newRequest.setDisplayOrder(2);

        Product savedProduct = new Product(
                "Banana",
                "Fresh Farm",
                "SKU002",
                "Fresh Bananas",
                "Fruits",
                "fruits",
                List.of("banana.jpg"),
                "USD",
                new BigDecimal("5.00"),
                new BigDecimal("6.00"),
                "1 kg",
                30,
                2
        );

        savedProduct.setId("2");

        when(productRepository.existsBySku("SKU001"))
                .thenReturn(true);

        when(productRepository.existsBySku("SKU002"))
                .thenReturn(false);

        when(productRepository.save(any(Product.class)))
                .thenReturn(savedProduct);

        List<ProductResponse> result =
                productService.addProductsBulk(
                        List.of(existingRequest, newRequest)
                );

        assertNotNull(result);
        assertEquals(1, result.size());

        assertEquals("SKU002", result.get(0).getSku());
        assertEquals("Banana", result.get(0).getName());

        verify(productRepository).existsBySku("SKU001");
        verify(productRepository).existsBySku("SKU002");

        verify(productRepository, times(1))
                .save(any(Product.class));
    }

    @Test
    void addProductsBulk_ShouldNotUpdateExistingProduct() {

        when(productRepository.existsBySku("SKU001"))
                .thenReturn(true);

        List<ProductResponse> result =
                productService.addProductsBulk(List.of(createRequest));

        assertTrue(result.isEmpty());

        verify(productRepository).existsBySku("SKU001");

        verify(productRepository, never())
                .save(any(Product.class));
    }

    @Test
    void addProductsBulk_ShouldReturnEmptyList_WhenRequestIsEmpty() {

        List<ProductResponse> result =
                productService.addProductsBulk(Collections.emptyList());

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verifyNoInteractions(productRepository);
    }
}