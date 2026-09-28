package com.shippex.controller;

import com.shippex.constants.AccountStatus;
import com.shippex.constants.Role;
import com.shippex.dto.order.PlaceOrderRequest;
import com.shippex.dto.order.UpdateOrderStatusRequest;
import com.shippex.model.AppUser;
import com.shippex.model.Order;
import com.shippex.model.OrderStatus;
import com.shippex.security.CustomUserDetails;
import com.shippex.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
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
class OrderControllersTest {
    @Mock OrderService orderService;
    @InjectMocks OrderController orderController;
    @InjectMocks AdminOrderController adminOrderController;

    private CustomUserDetails user;

    @BeforeEach
    void setUp() {
        AppUser appUser = new AppUser();
        appUser.setId("user-1");
        appUser.setUsername("buyer");
        appUser.setRole(Role.USER);
        appUser.setAccountStatus(AccountStatus.ACTIVE);
        user = new CustomUserDetails(appUser);
    }

    @Test
    void placeOrder_usesAuthenticatedCustomerAndReturnsCreated() {
        PlaceOrderRequest request = new PlaceOrderRequest();
        request.setItems(List.of());
        Order order = order("order-1", "user-1", OrderStatus.PLACED);
        when(orderService.placeOrder("user-1", request)).thenReturn(order);

        var response = orderController.placeOrder(user, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("order-1", response.getBody().getData().getId());
        verify(orderService).placeOrder("user-1", request);
    }

    @Test
    void getMyOrders_mapsTheAuthenticatedUsersOrders() {
        when(orderService.getOrdersForUser("user-1"))
                .thenReturn(List.of(order("order-1", "user-1", OrderStatus.PLACED)));

        var response = orderController.getMyOrders(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getData().size());
        assertEquals("order-1", response.getBody().getData().getFirst().getId());
    }

    @Test
    void getMyOrders_returnsEmptyListWhenCustomerHasNoOrders() {
        when(orderService.getOrdersForUser("user-1")).thenReturn(List.of());
        assertTrue(orderController.getMyOrders(user).getBody().getData().isEmpty());
    }

    @Test
    void getMyOrders_returnsTenByDefaultAndTheRequestedNextTwenty() {
        when(orderService.getOrdersForUser("user-1")).thenReturn(orders(35));

        var firstPage = orderController.getMyOrders(user);
        var secondPage = orderController.getMyOrders(user, 10, 20);

        assertEquals(10, firstPage.getBody().getData().size());
        assertEquals("order-0", firstPage.getBody().getData().getFirst().getId());
        assertEquals(20, secondPage.getBody().getData().size());
        assertEquals("order-10", secondPage.getBody().getData().getFirst().getId());
        assertEquals("order-29", secondPage.getBody().getData().getLast().getId());
    }

    @Test
    void cancelOrder_delegatesOrderAndAuthenticatedOwner() {
        when(orderService.cancelOrder("order-1", "user-1"))
                .thenReturn(order("order-1", "user-1", OrderStatus.CANCELLED));

        var response = orderController.cancelOrder("order-1", user);

        assertEquals(OrderStatus.CANCELLED, response.getBody().getData().getStatus());
        verify(orderService).cancelOrder("order-1", "user-1");
    }

    @Test
    void cancelOrder_propagatesOwnershipOrTransitionFailure() {
        when(orderService.cancelOrder("order-1", "user-1"))
                .thenThrow(new IllegalStateException("not cancellable"));
        assertThrows(IllegalStateException.class, () -> orderController.cancelOrder("order-1", user));
    }

    @Test
    void adminGetAllOrders_returnsMappedAdminResults() {
        when(orderService.getAllOrdersForAdmin())
                .thenReturn(List.of(com.shippex.dto.order.OrderResponse.builder().id("order-1").build()));
        var response = adminOrderController.getAllOrders();
        assertEquals("order-1", response.getBody().getData().getFirst().getId());
    }

    @Test
    void adminGetAllOrders_returnsTenByDefaultAndTheRequestedNextTwenty() {
        when(orderService.getAllOrdersForAdmin()).thenReturn(orderResponses(35));

        var firstPage = adminOrderController.getAllOrders();
        var secondPage = adminOrderController.getAllOrders(10, 20);

        assertEquals(10, firstPage.getBody().getData().size());
        assertEquals("order-0", firstPage.getBody().getData().getFirst().getId());
        assertEquals(20, secondPage.getBody().getData().size());
        assertEquals("order-10", secondPage.getBody().getData().getFirst().getId());
        assertEquals("order-29", secondPage.getBody().getData().getLast().getId());
    }

    @Test
    void adminUpdateStatus_delegatesRequestAndMapsUpdatedOrder() {
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
        request.setStatus(OrderStatus.CONFIRMED);
        when(orderService.updateStatus("order-1", request))
                .thenReturn(order("order-1", "user-1", OrderStatus.CONFIRMED));

        var response = adminOrderController.updateStatus("order-1", request);

        assertEquals(OrderStatus.CONFIRMED, response.getBody().getData().getStatus());
        verify(orderService).updateStatus("order-1", request);
    }

    private Order order(String id, String userId, OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setUserId(userId);
        order.setOrderNumber("ORD-1");
        order.setStatus(status);
        return order;
    }

    private List<Order> orders(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> order("order-" + index, "user-1", OrderStatus.PLACED))
                .toList();
    }

    private List<com.shippex.dto.order.OrderResponse> orderResponses(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> com.shippex.dto.order.OrderResponse.builder().id("order-" + index).build())
                .toList();
    }
}
