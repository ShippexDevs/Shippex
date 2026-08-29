package com.shippex.service.impl;

import com.shippex.dto.order.DeliveryDestinationRequest;
import com.shippex.dto.order.OrderItemRequest;
import com.shippex.dto.order.PlaceOrderRequest;
import com.shippex.dto.order.UpdateOrderStatusRequest;
import com.shippex.exception.InsufficientStockException;
import com.shippex.exception.InvalidOrderStatusException;
import com.shippex.exception.OrderNotFoundException;
import com.shippex.exception.ProductNotFoundException;
import com.shippex.model.Order;
import com.shippex.model.OrderItem;
import com.shippex.model.OrderStatus;
import com.shippex.model.Product;
import com.shippex.repository.OrderRepository;
import com.shippex.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private OrderServiceImpl orderService;

    private Product product;
    private PlaceOrderRequest request;

    @BeforeEach
    void setUp() {
        product = product("product-1", "USD", 10, true, List.of("image.jpg"));
        request = orderRequest(List.of(itemRequest("product-1", 2)));
    }

    @Test
    void placeOrder_ShouldCreateOrderSnapshotAndReduceStock() {
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder("user-1", request);

        assertTrue(result.getOrderNumber().matches("ORD-[A-F0-9]{12}"));
        assertEquals("user-1", result.getUserId());
        assertEquals(OrderStatus.PLACED, result.getStatus());
        assertEquals(new BigDecimal("20.00"), result.getTotalAmount());
        assertEquals("USD", result.getCurrency());
        assertEquals(8, product.getStock());
        assertEquals(1, result.getItems().size());
        assertEquals("SKU-product-1", result.getItems().getFirst().getSku());
        assertEquals(new BigDecimal("20.00"), result.getItems().getFirst().getSubtotal());
        assertEquals("MV Example", result.getDeliveryDestination().getShipName());
        assertEquals("Mumbai Port", result.getDeliveryDestination().getPortName());
        assertEquals("1234567", result.getDeliveryDestination().getImoNumber());
        assertEquals(LocalDate.of(2026, 8, 27), result.getEstimatedDeliveryDate());
        assertEquals(LocalTime.of(14, 30), result.getEstimatedDeliveryTime());
        verify(productRepository).saveAll(any());
        verify(orderRepository).save(result);
    }

    @Test
    void placeOrder_ShouldCalculateTotalForMultipleProducts() {
        Product secondProduct = product("product-2", "USD", 5, true, List.of("second.jpg"));
        secondProduct.setCurrentPrice(new BigDecimal("7.50"));
        request.setItems(List.of(itemRequest("product-1", 2), itemRequest("product-2", 3)));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(productRepository.findById("product-2")).thenReturn(Optional.of(secondProduct));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder("user-1", request);

        assertEquals(2, result.getItems().size());
        assertEquals(new BigDecimal("42.50"), result.getTotalAmount());
        assertEquals(8, product.getStock());
        assertEquals(2, secondProduct.getStock());
        verify(productRepository).saveAll(any());
    }

    @Test
    void placeOrder_ShouldAllowProductWithoutImages() {
        product.setImages(Collections.emptyList());
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder("user-1", request);

        assertNull(result.getItems().getFirst().getImage());
    }

    @Test
    void placeOrder_ShouldPersistNullOptionalDeliveryFields() {
        request.setEstimatedDeliveryDate(null);
        request.setEstimatedDeliveryTime(null);
        request.setDeliveryInstructions(null);
        request.setOrderInstructions(null);
        request.getDeliveryDestination().setImoNumber(null);
        request.getDeliveryDestination().setBerthNumber(null);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.placeOrder("user-1", request);

        assertNull(result.getEstimatedDeliveryDate());
        assertNull(result.getEstimatedDeliveryTime());
        assertNull(result.getDeliveryInstructions());
        assertNull(result.getOrderInstructions());
        assertNull(result.getDeliveryDestination().getImoNumber());
        assertNull(result.getDeliveryDestination().getBerthNumber());
    }

    @Test
    void placeOrder_ShouldThrowWhenProductDoesNotExist() {
        when(productRepository.findById("product-1")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> orderService.placeOrder("user-1", request));

        verify(productRepository, never()).saveAll(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_ShouldThrowWhenProductIsInactive() {
        product.setActive(false);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder("user-1", request));

        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void placeOrder_ShouldThrowWhenStockIsNullOrInsufficient() {
        product.setStock(null);
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));

        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder("user-1", request));

        product.setStock(1);
        assertThrows(InsufficientStockException.class, () -> orderService.placeOrder("user-1", request));
        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void placeOrder_ShouldRejectProductsWithDifferentCurrencies() {
        Product secondProduct = product("product-2", "EUR", 5, true, List.of("second.jpg"));
        request.setItems(List.of(itemRequest("product-1", 1), itemRequest("product-2", 1)));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(productRepository.findById("product-2")).thenReturn(Optional.of(secondProduct));

        assertThrows(IllegalArgumentException.class, () -> orderService.placeOrder("user-1", request));

        verify(productRepository, never()).saveAll(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void getOrdersForUser_ShouldReturnRepositoryOrders() {
        List<Order> orders = List.of(order("order-1", "user-1", OrderStatus.PLACED));
        when(orderRepository.findByUserIdOrderByCreatedAtDesc("user-1")).thenReturn(orders);

        assertSame(orders, orderService.getOrdersForUser("user-1"));
    }

    @Test
    void getAllOrders_ShouldReturnRepositoryOrders() {
        List<Order> orders = List.of(order("order-1", "user-1", OrderStatus.PLACED));
        when(orderRepository.findAllByOrderByCreatedAtDesc()).thenReturn(orders);

        assertSame(orders, orderService.getAllOrders());
    }

    @Test
    void cancelOrder_ShouldCancelPlacedOrderAndRestoreStock() {
        Order order = order("order-1", "user-1", OrderStatus.PLACED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.cancelOrder("order-1", "user-1");

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(12, product.getStock());
        verify(productRepository).saveAll(any());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_ShouldCancelConfirmedOrder() {
        Order order = order("order-1", "user-1", OrderStatus.CONFIRMED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.cancelOrder("order-1", "user-1");

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
    }

    @Test
    void cancelOrder_ShouldStillCancelWhenAnOrderedProductNoLongerExists() {
        Order order = order("order-1", "user-1", OrderStatus.PLACED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(productRepository.findById("product-1")).thenReturn(Optional.empty());
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.cancelOrder("order-1", "user-1");

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(productRepository).saveAll(any());
        verify(orderRepository).save(order);
    }

    @Test
    void cancelOrder_ShouldHideOrderOwnedByAnotherUser() {
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order("order-1", "user-2", OrderStatus.PLACED)));

        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder("order-1", "user-1"));

        verify(productRepository, never()).saveAll(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_ShouldRejectTerminalOrDeliveryOrders() {
        for (OrderStatus status : List.of(OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED, OrderStatus.CANCELLED)) {
            reset(orderRepository);
            when(orderRepository.findById("order-1")).thenReturn(Optional.of(order("order-1", "user-1", status)));

            assertThrows(InvalidOrderStatusException.class, () -> orderService.cancelOrder("order-1", "user-1"));
        }

        verify(productRepository, never()).saveAll(any());
    }

    @Test
    void cancelOrder_ShouldThrowWhenOrderDoesNotExist() {
        when(orderRepository.findById("order-1")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder("order-1", "user-1"));
    }

    @Test
    void updateStatus_ShouldUpdateNonTerminalOrder() {
        Order order = order("order-1", "user-1", OrderStatus.PLACED);
        UpdateOrderStatusRequest statusRequest = statusRequest(OrderStatus.CONFIRMED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.updateStatus("order-1", statusRequest);

        assertEquals(OrderStatus.CONFIRMED, result.getStatus());
        verify(productRepository, never()).saveAll(any());
        verify(orderRepository).save(order);
    }

    @Test
    void updateStatus_ShouldRestoreStockWhenAdminCancelsOrder() {
        Order order = order("order-1", "user-1", OrderStatus.CONFIRMED);
        when(orderRepository.findById("order-1")).thenReturn(Optional.of(order));
        when(productRepository.findById("product-1")).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Order result = orderService.updateStatus("order-1", statusRequest(OrderStatus.CANCELLED));

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        assertEquals(12, product.getStock());
        verify(productRepository).saveAll(any());
    }

    @Test
    void updateStatus_ShouldRejectDeliveredOrCancelledOrders() {
        for (OrderStatus status : List.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED)) {
            reset(orderRepository);
            when(orderRepository.findById("order-1")).thenReturn(Optional.of(order("order-1", "user-1", status)));

            assertThrows(InvalidOrderStatusException.class,
                    () -> orderService.updateStatus("order-1", statusRequest(OrderStatus.CONFIRMED)));
        }

        verify(orderRepository, never()).save(any());
    }

    @Test
    void updateStatus_ShouldThrowWhenOrderDoesNotExist() {
        when(orderRepository.findById("order-1")).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.updateStatus("order-1", statusRequest(OrderStatus.CONFIRMED)));
    }

    private Product product(String id, String currency, Integer stock, boolean active, List<String> images) {
        Product product = new Product();
        product.setId(id);
        product.setName("Product " + id);
        product.setSku("SKU-" + id);
        product.setCurrentPrice(new BigDecimal("10.00"));
        product.setCurrency(currency);
        product.setStock(stock);
        product.setActive(active);
        product.setImages(images);
        product.setUnit("each");
        return product;
    }

    private PlaceOrderRequest orderRequest(List<OrderItemRequest> items) {
        DeliveryDestinationRequest destination = new DeliveryDestinationRequest();
        destination.setShipName("MV Example");
        destination.setImoNumber("1234567");
        destination.setBerthNumber("B-12");
        destination.setPortName("Mumbai Port");

        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setItems(items);
        request.setDeliveryDestination(destination);
        request.setEstimatedDeliveryDate(LocalDate.of(2026, 8, 27));
        request.setEstimatedDeliveryTime(LocalTime.of(14, 30));
        request.setDeliveryInstructions("Call before arrival");
        request.setOrderInstructions("Keep items dry");
        request.setPaymentMethod("CASH_ON_DELIVERY");
        return request;
    }

    private OrderItemRequest itemRequest(String productId, int quantity) {
        OrderItemRequest item = new OrderItemRequest();
        item.setProductId(productId);
        item.setQuantity(quantity);
        return item;
    }

    private UpdateOrderStatusRequest statusRequest(OrderStatus status) {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(status);
        return request;
    }

    private Order order(String id, String userId, OrderStatus status) {
        OrderItem item = new OrderItem();
        item.setProductId("product-1");
        item.setQuantity(2);
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        order.setStatus(status);
        order.setItems(List.of(item));
        return order;
    }
}
