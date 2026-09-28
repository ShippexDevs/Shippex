package com.shippex.controller;

import com.shippex.dto.dashboard.DashboardWidgetsResponse;
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
}
