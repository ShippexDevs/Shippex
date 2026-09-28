package com.shippex.controller;

import com.shippex.dto.product.CreateProductRequest;
import com.shippex.dto.product.ProductResponse;
import com.shippex.dto.product.UpdateProductRequest;
import com.shippex.model.Product;
import com.shippex.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {
    @Mock ProductService productService;
    @InjectMocks ProductController controller;

    @Test
    void addGetUpdateAndDeleteMapProductsAndPreserveHttpStatuses() {
        Product product = product("p-1", "Gloves");
        CreateProductRequest createRequest = new CreateProductRequest();
        UpdateProductRequest updateRequest = new UpdateProductRequest();
        when(productService.addProduct(createRequest)).thenReturn(product);
        when(productService.getProductById("p-1")).thenReturn(product);
        when(productService.updateProductById("p-1", updateRequest)).thenReturn(product);
        when(productService.deleteProductById("p-1")).thenReturn(product);

        var created = controller.addProduct(createRequest);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertEquals("p-1", created.getBody().getId());
        assertEquals("Gloves", controller.getProductById("p-1").getBody().getName());
        assertEquals("p-1", controller.updateProduct("p-1", updateRequest).getBody().getId());
        assertEquals("p-1", controller.deleteProduct("p-1").getBody().getId());
    }

    @Test
    void productEndpointsPropagateNotFoundAndValidationServiceErrors() {
        when(productService.getProductById("missing")).thenThrow(new IllegalArgumentException("not found"));
        assertThrows(IllegalArgumentException.class, () -> controller.getProductById("missing"));
        verify(productService).getProductById("missing");
    }

    @Test
    void categoryAndFeaturedEndpointsReturnMappedProductsAndSupportEmptyResults() {
        when(productService.getProductsByCategorySlug("deck-supplies"))
                .thenReturn(List.of(product("p-1", "Gloves")));
        when(productService.getFeaturedProducts()).thenReturn(List.of());

        var categoryResponse = controller.getProductsByCategorySlug("deck-supplies");
        assertEquals("Gloves", categoryResponse.getBody().getFirst().getName());
        assertTrue(controller.getFeaturedProducts().getBody().isEmpty());
        verify(productService).getProductsByCategorySlug("deck-supplies");
    }

    @Test
    void bulkEndpointReturnsServiceResponsesIncludingEmptyList() {
        List<CreateProductRequest> request = List.of(new CreateProductRequest());
        List<ProductResponse> response = List.of(ProductResponse.builder().id("p-1").build());
        when(productService.addProductsBulk(request)).thenReturn(response);
        assertSame(response, controller.addProductsBulk(request).getBody());
        when(productService.addProductsBulk(List.of())).thenReturn(List.of());
        assertTrue(controller.addProductsBulk(List.of()).getBody().isEmpty());
    }

    private Product product(String id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }
}
