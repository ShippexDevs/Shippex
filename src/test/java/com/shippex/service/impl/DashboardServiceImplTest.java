package com.shippex.service.impl;

import com.shippex.dto.dashboard.AdminOrderResponse;
import com.shippex.dto.dashboard.DailyOrderOverviewResponse;
import com.shippex.dto.dashboard.DashboardWidgetsResponse;
import com.shippex.dto.dashboard.RecentActivityResponse;
import com.shippex.model.AppUser;
import com.shippex.model.Order;
import com.shippex.model.OrderStatus;
import com.shippex.model.Product;
import com.shippex.repository.AppUserRepository;
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
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private ProductRepository productRepository;
    @InjectMocks
    private DashboardServiceImpl dashboardService;

    private Order order;
    private Product product;
    private AppUser user;

    @BeforeEach
    void setUp() {
        order = order("order-1", "ORD-001", "user-1", OrderStatus.DELIVERED, new BigDecimal("100.00"));
        product = product("product-1", "Product A", "Food");
        user = user("user-1", "John Doe", "johndoe");
    }

    @Test
    void getWidgets_ShouldCalculateMetricsRevenueAndDistributions() {
        Order cancelled = order("order-2", "ORD-002", "user-2", OrderStatus.CANCELLED, new BigDecimal("20.00"));
        when(orderRepository.count()).thenReturn(12L);
        when(orderRepository.countByCreatedAtBetween(any(), any())).thenReturn(4L, 2L);
        when(appUserRepository.count()).thenReturn(8L);
        when(appUserRepository.countByCreatedAtBetween(any(), any())).thenReturn(3L, 1L);
        when(productRepository.count()).thenReturn(6L);
        when(productRepository.countByCreatedAtBetween(any(), any())).thenReturn(2L, 1L);
        when(orderRepository.findByStatusNot(OrderStatus.CANCELLED)).thenReturn(List.of(order));
        when(orderRepository.findByCreatedAtBetween(any(), any())).thenReturn(List.of(order), List.of(cancelled));
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(orderRepository.findAll()).thenReturn(List.of(order, cancelled));

        DashboardWidgetsResponse result = dashboardService.getWidgets();

        assertEquals(new BigDecimal("12"), result.getOrders().getTotal());
        assertEquals(new BigDecimal("2"), result.getOrders().getChangeSinceLastMonth());
        assertEquals(new BigDecimal("100.00"), result.getRevenue().getTotal());
        assertEquals(new BigDecimal("100.00"), result.getRevenue().getChangeSinceLastMonth());
        assertEquals("Food", result.getProductCategoryDistribution().getFirst().getLabel());
        assertEquals(100.0, result.getProductCategoryDistribution().getFirst().getPercentage());
        assertEquals(2, result.getOrderStatusDistribution().size());
    }

    @Test
    void getOverviewChart_ShouldIncludeEachRequestedDayIncludingZeroOrderDays() {
        order.setCreatedAt(LocalDateTime.now().minusDays(1));
        when(orderRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(any()))
                .thenReturn(List.of(order));

        List<DailyOrderOverviewResponse> result = dashboardService.getOverviewChart(3);

        assertEquals(3, result.size());
        assertEquals(LocalDate.now().minusDays(2), result.get(0).getDate());
        assertEquals(1, result.get(1).getOrderCount());
        assertEquals(0, result.get(2).getOrderCount());
    }

    @Test
    void getRecentOrders_ShouldIncludeTheUserWhoPlacedEachOrder() {
        when(appUserRepository.findAll()).thenReturn(List.of(user));
        when(orderRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(any())).thenReturn(List.of(order));

        List<AdminOrderResponse> result = dashboardService.getRecentOrders(7);

        assertEquals(1, result.size());
        assertEquals("ORD-001", result.getFirst().getOrder().getOrderNumber());
        assertEquals("johndoe", result.getFirst().getUser().getUsername());
    }

    @Test
    void getRecentActivity_ShouldMergeAndSortActivitySources() {
        LocalDateTime old = LocalDateTime.now().minusHours(2);
        LocalDateTime recent = LocalDateTime.now().minusHours(1);
        order.setCreatedAt(old);
        order.setUpdatedAt(recent);
        product.setCreatedAt(old);
        product.setUpdatedAt(recent);
        user.setCreatedAt(old);
        user.setLastUpdatedAt(recent);
        when(orderRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(any())).thenReturn(List.of(order));
        when(orderRepository.findByUpdatedAtGreaterThanEqual(any())).thenReturn(List.of(order));
        when(productRepository.findByCreatedAtGreaterThanEqual(any())).thenReturn(List.of(product));
        when(productRepository.findByUpdatedAtGreaterThanEqual(any())).thenReturn(List.of(product));
        when(appUserRepository.findByCreatedAtGreaterThanEqual(any())).thenReturn(List.of(user));
        when(appUserRepository.findByLastUpdatedAtGreaterThanEqual(any())).thenReturn(List.of(user));

        List<RecentActivityResponse> result = dashboardService.getRecentActivity(7);

        assertEquals(6, result.size());
        assertEquals(recent, result.getFirst().getOccurredAt());
        assertTrue(result.stream().anyMatch(item -> item.getType().equals("ORDER_UPDATED")));
        assertTrue(result.stream().anyMatch(item -> item.getType().equals("USER_REGISTERED")));
    }

    @Test
    void timeBasedEndpoints_ShouldRejectInvalidDayCounts() {
        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> dashboardService.getOverviewChart(0)),
                () -> assertThrows(IllegalArgumentException.class, () -> dashboardService.getRecentOrders(366)),
                () -> assertThrows(IllegalArgumentException.class, () -> dashboardService.getRecentActivity(0))
        );
    }

    private Order order(String id, String orderNumber, String userId, OrderStatus status, BigDecimal total) {
        Order order = new Order();
        order.setId(id);
        order.setOrderNumber(orderNumber);
        order.setUserId(userId);
        order.setStatus(status);
        order.setTotalAmount(total);
        return order;
    }

    private Product product(String id, String name, String category) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setCategory(category);
        return product;
    }

    private AppUser user(String id, String name, String username) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setName(name);
        user.setUsername(username);
        return user;
    }
}
