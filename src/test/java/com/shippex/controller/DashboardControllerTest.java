package com.shippex.controller;

import com.shippex.dto.dashboard.DashboardWidgetsResponse;
import com.shippex.dto.dashboard.AdminOrderResponse;
import com.shippex.dto.order.OrderResponse;
import com.shippex.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {
    @Mock DashboardService dashboardService;
    @InjectMocks DashboardController controller;

    @Test
    void widgetsWrapServiceResult() {
        DashboardWidgetsResponse result = DashboardWidgetsResponse.builder().build();
        when(dashboardService.getWidgets()).thenReturn(result);
        assertSame(result, controller.getWidgets().getBody().getData());
    }

    @Test
    void dateRangeEndpointsForwardRequestedDayCount() {
        when(dashboardService.getOverviewChart(7)).thenReturn(List.of());
        when(dashboardService.getRecentOrders(7)).thenReturn(List.of());
        when(dashboardService.getRecentActivity(7)).thenReturn(List.of());
        assertTrue(controller.getOverviewChart(7).getBody().getData().isEmpty());
        assertTrue(controller.getRecentOrders(7).getBody().getData().isEmpty());
        assertTrue(controller.getRecentActivity(7).getBody().getData().isEmpty());
        verify(dashboardService).getOverviewChart(7);
        verify(dashboardService).getRecentOrders(7);
        verify(dashboardService).getRecentActivity(7);
    }

    @Test
    void dashboardServiceFailuresPropagate() {
        when(dashboardService.getRecentActivity(1)).thenThrow(new IllegalArgumentException("days out of range"));
        assertThrows(IllegalArgumentException.class, () -> controller.getRecentActivity(1));
    }

    @Test
    void recentOrdersReturnsTenByDefaultAndTheRequestedNextTwenty() {
        List<AdminOrderResponse> orders = java.util.stream.IntStream.range(0, 35)
                .mapToObj(index -> AdminOrderResponse.builder()
                        .order(OrderResponse.builder().id("order-" + index).build())
                        .build())
                .toList();
        when(dashboardService.getRecentOrders(7)).thenReturn(orders);

        var firstPage = controller.getRecentOrders(7);
        var secondPage = controller.getRecentOrders(7, 10, 20);

        assertEquals(10, firstPage.getBody().getData().size());
        assertEquals("order-0", firstPage.getBody().getData().getFirst().getOrder().getId());
        assertEquals(20, secondPage.getBody().getData().size());
        assertEquals("order-10", secondPage.getBody().getData().getFirst().getOrder().getId());
        assertEquals("order-29", secondPage.getBody().getData().getLast().getOrder().getId());
    }
}
