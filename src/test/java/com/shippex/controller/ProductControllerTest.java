package com.shippex.controller;

import com.shippex.dto.product.CreateProductRequest;
import com.shippex.dto.product.ProductResponse;
import com.shippex.dto.product.UpdateProductRequest;
import com.shippex.dto.product.UpdateStockRequest;
import com.shippex.dto.product.UpdateFeaturedRequest;
import com.shippex.dto.product.UpdateActiveRequest;
import com.shippex.model.Product;
import com.shippex.model.AppUser;
import com.shippex.constants.AccountStatus;
import com.shippex.constants.Role;
import com.shippex.security.CustomUserDetails;
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
    @InjectMocks AdminProductController adminController;

    @Test
    void addGetUpdateAndDeleteMapProductsAndPreserveHttpStatuses() {
        Product product = product("p-1", "Gloves");
        CreateProductRequest createRequest = new CreateProductRequest();
        UpdateProductRequest updateRequest = new UpdateProductRequest();
        when(productService.addProduct(createRequest)).thenReturn(product);
        when(productService.getProductById("p-1")).thenReturn(product);
        when(productService.updateProductById("p-1", updateRequest)).thenReturn(product);
        when(productService.deleteProductById("p-1")).thenReturn(product);

        var created = adminController.addProduct(createRequest);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        assertEquals("p-1", created.getBody().getId());
        assertEquals("Gloves", controller.getProductById("p-1").getBody().getName());
        assertEquals("p-1", adminController.updateProduct("p-1", updateRequest).getBody().getId());
        assertEquals("p-1", adminController.deleteProduct("p-1").getBody().getId());
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
    void publicProductRequestsAreLimitedToFirstTenEvenWhenLaterPagesAreRequested() {
        List<Product> products = products(35);
        when(productService.getProductsByCategorySlug("deck-supplies")).thenReturn(products);
        when(productService.getFeaturedProducts()).thenReturn(products);

        var categoryPage = controller.getProductsByCategorySlug("deck-supplies", 10, 20, null);
        var featuredPage = controller.getFeaturedProducts(10, 20, null);

        assertEquals(10, categoryPage.getBody().size());
        assertEquals("p-0", categoryPage.getBody().getFirst().getId());
        assertEquals(10, featuredPage.getBody().size());
        assertEquals("p-0", featuredPage.getBody().getFirst().getId());
    }

    @Test
    void authenticatedAppUserCanRequestTheNextTwentyProducts() {
        AppUser appUser = new AppUser();
        appUser.setId("user-1");
        appUser.setUsername("buyer");
        appUser.setRole(Role.USER);
        appUser.setAccountStatus(AccountStatus.ACTIVE);
        CustomUserDetails principal = new CustomUserDetails(appUser);
        List<Product> products = products(35);
        when(productService.getProductsByCategorySlug("deck-supplies")).thenReturn(products);
        when(productService.getFeaturedProducts()).thenReturn(products);

        var categoryPage = controller.getProductsByCategorySlug("deck-supplies", 10, 20, principal);
        var featuredPage = controller.getFeaturedProducts(10, 20, principal);

        assertEquals(20, categoryPage.getBody().size());
        assertEquals("p-10", categoryPage.getBody().getFirst().getId());
        assertEquals("p-29", categoryPage.getBody().getLast().getId());
        assertEquals(20, featuredPage.getBody().size());
        assertEquals("p-10", featuredPage.getBody().getFirst().getId());
        assertEquals("p-29", featuredPage.getBody().getLast().getId());
    }

    @Test
    void bulkEndpointReturnsServiceResponsesIncludingEmptyList() {
        List<CreateProductRequest> request = List.of(new CreateProductRequest());
        List<ProductResponse> response = List.of(ProductResponse.builder().id("p-1").build());
        when(productService.addProductsBulk(request)).thenReturn(response);
        assertSame(response, adminController.addProductsBulk(request).getBody());
        when(productService.addProductsBulk(List.of())).thenReturn(List.of());
        assertTrue(adminController.addProductsBulk(List.of()).getBody().isEmpty());
    }

    @Test
    void adminFieldEndpointsDelegateAndReturnMappedProducts() {
        Product stockProduct = product("p-1", "Gloves");
        stockProduct.setStock(12);
        Product featuredProduct = product("p-1", "Gloves");
        featuredProduct.setFeatured(true);
        Product activeProduct = product("p-1", "Gloves");
        activeProduct.setActive(false);
        when(productService.updateProductStock("p-1", 12)).thenReturn(stockProduct);
        when(productService.updateProductFeatured("p-1", true)).thenReturn(featuredProduct);
        when(productService.updateProductActive("p-1", false)).thenReturn(activeProduct);

        assertEquals(12, adminController.updateProductStock("p-1", new UpdateStockRequest(12)).getBody().getStock());
        assertTrue(adminController.updateProductFeatured("p-1", new UpdateFeaturedRequest(true)).getBody().getFeatured());
        assertFalse(adminController.updateProductActive("p-1", new UpdateActiveRequest(false)).getBody().getActive());
        verify(productService).updateProductStock("p-1", 12);
        verify(productService).updateProductFeatured("p-1", true);
        verify(productService).updateProductActive("p-1", false);
    }

    @Test
    void adminGetAllProductsReturnsMappedResultsAndSupportsOffsetPagination() {
        List<Product> products = products(35);
        products.getFirst().setActive(false);
        products.getFirst().setFeatured(false);
        when(productService.getAllProductsForAdmin()).thenReturn(products);

        var firstPage = adminController.getAllProducts(0, 10);
        var secondPage = adminController.getAllProducts(10, 20);

        assertEquals(HttpStatus.OK, firstPage.getStatusCode());
        assertTrue(firstPage.getBody().isSuccess());
        assertEquals(10, firstPage.getBody().getData().size());
        assertEquals("p-0", firstPage.getBody().getData().getFirst().getId());
        assertFalse(firstPage.getBody().getData().getFirst().getActive());
        assertFalse(firstPage.getBody().getData().getFirst().getFeatured());
        assertEquals(20, secondPage.getBody().getData().size());
        assertEquals("p-10", secondPage.getBody().getData().getFirst().getId());
        assertEquals("p-29", secondPage.getBody().getData().getLast().getId());
        verify(productService, times(2)).getAllProductsForAdmin();
    }

    private Product product(String id, String name) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        return product;
    }

    private List<Product> products(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> product("p-" + index, "Product " + index))
                .toList();
    }
}
