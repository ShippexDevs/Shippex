package com.shippex.service.impl;

import com.shippex.dto.dashboard.AdminOrderResponse;
import com.shippex.dto.dashboard.DailyOrderOverviewResponse;
import com.shippex.dto.dashboard.DashboardMetricResponse;
import com.shippex.dto.dashboard.DashboardWidgetsResponse;
import com.shippex.dto.dashboard.DistributionResponse;
import com.shippex.dto.dashboard.OrderUserResponse;
import com.shippex.dto.dashboard.RecentActivityResponse;
import com.shippex.mapper.OrderMapper;
import com.shippex.model.AppUser;
import com.shippex.model.Order;
import com.shippex.model.OrderStatus;
import com.shippex.model.Product;
import com.shippex.repository.AppUserRepository;
import com.shippex.repository.OrderRepository;
import com.shippex.repository.ProductRepository;
import com.shippex.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardServiceImpl implements DashboardService {
    private final OrderRepository orderRepository;
    private final AppUserRepository appUserRepository;
    private final ProductRepository productRepository;

    @Override
    public DashboardWidgetsResponse getWidgets() {
        LocalDateTime currentMonthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime previousMonthStart = currentMonthStart.minusMonths(1);
        LocalDateTime now = LocalDateTime.now();
        log.debug("Building dashboard widgets for currentPeriod={} to {} and previousPeriod={} to {}",
                currentMonthStart, now, previousMonthStart, currentMonthStart);

        List<Order> nonCancelledOrders = orderRepository.findByStatusNot(OrderStatus.CANCELLED);
        List<Order> currentMonthOrders = orderRepository
                .findByCreatedAtBetween(currentMonthStart, now);
        List<Order> previousMonthOrders = orderRepository
                .findByCreatedAtBetween(previousMonthStart, currentMonthStart);

        DashboardWidgetsResponse response = DashboardWidgetsResponse.builder()
                .orders(countMetric(orderRepository.count(),
                        orderRepository.countByCreatedAtBetween(currentMonthStart, now),
                        orderRepository.countByCreatedAtBetween(previousMonthStart, currentMonthStart)))
                .users(countMetric(appUserRepository.count(),
                        appUserRepository.countByCreatedAtBetween(currentMonthStart, LocalDateTime.now()),
                        appUserRepository.countByCreatedAtBetween(previousMonthStart, currentMonthStart)))
                .products(countMetric(productRepository.count(),
                        productRepository.countByCreatedAtBetween(currentMonthStart, LocalDateTime.now()),
                        productRepository.countByCreatedAtBetween(previousMonthStart, currentMonthStart)))
                .revenue(revenueMetric(nonCancelledOrders, currentMonthOrders, previousMonthOrders))
                .productCategoryDistribution(distribution(productRepository.findAll(), product -> product.getCategory()))
                .orderStatusDistribution(distribution(orderRepository.findAll(), order ->
                        order.getStatus() == null ? "UNKNOWN" : order.getStatus().name()))
                .build();
        log.info("Dashboard widgets generated: totalOrders={}, totalUsers={}, totalProducts={}, totalRevenue={}",
                response.getOrders().getTotal(), response.getUsers().getTotal(), response.getProducts().getTotal(),
                response.getRevenue().getTotal());
        return response;
    }

    @Override
    public List<DailyOrderOverviewResponse> getOverviewChart(int days) {
        validateDays(days);
        LocalDate startDate = LocalDate.now().minusDays(days - 1L);
        log.debug("Building order overview chart for days={}, startDate={}", days, startDate);
        Map<LocalDate, Long> countsByDate = orderRepository
                .findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(startDate.atStartOfDay())
                .stream()
                .filter(order -> order.getCreatedAt() != null)
                .collect(Collectors.groupingBy(order -> order.getCreatedAt().toLocalDate(), Collectors.counting()));

        List<DailyOrderOverviewResponse> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(LocalDate.now()); date = date.plusDays(1)) {
            result.add(DailyOrderOverviewResponse.builder()
                    .date(date)
                    .orderCount(countsByDate.getOrDefault(date, 0L))
                    .build());
        }
        log.info("Order overview chart generated for days={} with {} data points", days, result.size());
        return result;
    }

    @Override
    public List<AdminOrderResponse> getRecentOrders(int days) {
        validateDays(days);
        log.debug("Fetching dashboard recent orders for days={}", days);
        Map<String, AppUser> usersById = appUserRepository.findAll().stream()
                .collect(Collectors.toMap(AppUser::getId, Function.identity()));
        List<AdminOrderResponse> orders = orderRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(cutoff(days)).stream()
                .map(order -> AdminOrderResponse.builder()
                        .order(OrderMapper.toResponse(order))
                        .user(toUserResponse(usersById.get(order.getUserId())))
                        .build())
                .toList();
        log.info("Dashboard recent orders retrieved: days={}, orderCount={}", days, orders.size());
        return orders;
    }

    @Override
    public List<RecentActivityResponse> getRecentActivity(int days) {
        validateDays(days);
        LocalDateTime cutoff = cutoff(days);
        log.debug("Building dashboard recent activity for days={}, cutoff={}", days, cutoff);
        List<RecentActivityResponse> activity = new ArrayList<>();
        Map<String, AppUser> usersById = appUserRepository.findAll().stream()
                .collect(Collectors.toMap(AppUser::getId, Function.identity()));

        for (Order order : orderRepository.findByCreatedAtGreaterThanEqualOrderByCreatedAtDesc(cutoff)) {
            AppUser user = usersById.get(order.getUserId());
            activity.add(activity("ORDER_PLACED", "Order " + displayOrderNumber(order)
                    + " was placed by " + displayUser(user) + ".", order.getCreatedAt()));
        }
        for (Order order : orderRepository.findByUpdatedAtGreaterThanEqual(cutoff)) {
            if (isLaterUpdate(order.getCreatedAt(), order.getUpdatedAt())) {
                activity.add(activity("ORDER_UPDATED", "Order " + displayOrderNumber(order) + " status changed to " + order.getStatus() + ".", order.getUpdatedAt()));
            }
        }
        for (Product product : productRepository.findByCreatedAtGreaterThanEqual(cutoff)) {
            activity.add(activity("PRODUCT_CREATED", "Product \"" + product.getName() + "\" was added.", product.getCreatedAt()));
        }
        for (Product product : productRepository.findByUpdatedAtGreaterThanEqual(cutoff)) {
            if (isLaterUpdate(product.getCreatedAt(), product.getUpdatedAt())) {
                activity.add(activity("PRODUCT_UPDATED", "Product \"" + product.getName() + "\" was updated.", product.getUpdatedAt()));
            }
        }
        for (AppUser user : appUserRepository.findByCreatedAtGreaterThanEqual(cutoff)) {
            activity.add(activity("USER_REGISTERED", "User " + user.getUsername() + " registered.", user.getCreatedAt()));
        }
        for (AppUser user : appUserRepository.findByLastUpdatedAtGreaterThanEqual(cutoff)) {
            if (isLaterUpdate(user.getCreatedAt(), user.getLastUpdatedAt())) {
                activity.add(activity("USER_UPDATED", "User " + user.getUsername() + " updated their profile.", user.getLastUpdatedAt()));
            }
        }
        List<RecentActivityResponse> recentActivity = activity.stream()
                .filter(item -> item.getOccurredAt() != null)
                .sorted(Comparator.comparing(RecentActivityResponse::getOccurredAt).reversed())
                .toList();
        log.info("Dashboard recent activity generated: days={}, activityCount={}", days, recentActivity.size());
        return recentActivity;
    }

    private DashboardMetricResponse countMetric(long total, long currentMonth, long previousMonth) {
        return metric(BigDecimal.valueOf(total), BigDecimal.valueOf(currentMonth), BigDecimal.valueOf(previousMonth));
    }

    private DashboardMetricResponse revenueMetric(List<Order> totalOrders, List<Order> currentOrders, List<Order> previousOrders) {
        return metric(sumRevenue(totalOrders), sumRevenue(currentOrders), sumRevenue(previousOrders));
    }

    private DashboardMetricResponse metric(BigDecimal total, BigDecimal currentMonth, BigDecimal previousMonth) {
        return DashboardMetricResponse.builder()
                .total(total)
                .currentMonth(currentMonth)
                .previousMonth(previousMonth)
                .changeSinceLastMonth(currentMonth.subtract(previousMonth))
                .build();
    }

    private BigDecimal sumRevenue(List<Order> orders) {
        return orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private <T> List<DistributionResponse> distribution(List<T> items, Function<T, String> labelFunction) {
        if (items.isEmpty()) {
            return List.of();
        }
        Map<String, Long> counts = items.stream().collect(Collectors.groupingBy(item -> {
            String label = labelFunction.apply(item);
            return label == null || label.isBlank() ? "Uncategorized" : label;
        }, Collectors.counting()));
        long total = items.size();
        return counts.entrySet().stream()
                .map(entry -> DistributionResponse.builder()
                        .label(entry.getKey())
                        .count(entry.getValue())
                        .percentage(BigDecimal.valueOf(entry.getValue() * 100.0 / total)
                                .setScale(2, RoundingMode.HALF_UP).doubleValue())
                        .build())
                .sorted(Comparator.comparing(DistributionResponse::getCount).reversed())
                .toList();
    }

    private OrderUserResponse toUserResponse(AppUser user) {
        if (user == null) {
            return null;
        }
        return OrderUserResponse.builder()
                .id(user.getId()).name(user.getName()).username(user.getUsername()).email(user.getEmail())
                .whatsappContactNo(user.getWhatsappContactNo()).shipName(user.getShipName())
                .shipIMONumber(user.getShipIMONumber()).build();
    }

    private RecentActivityResponse activity(String type, String description, LocalDateTime occurredAt) {
        return RecentActivityResponse.builder().type(type).description(description).occurredAt(occurredAt).build();
    }

    private boolean isLaterUpdate(LocalDateTime createdAt, LocalDateTime updatedAt) {
        return updatedAt != null && (createdAt == null || updatedAt.isAfter(createdAt));
    }

    private String displayOrderNumber(Order order) {
        return order.getOrderNumber() == null ? order.getId() : order.getOrderNumber();
    }

    private String displayUser(AppUser user) {
        if (user == null) {
            return "an unknown user";
        }
        return user.getUsername() == null ? user.getName() : user.getUsername();
    }

    private LocalDateTime cutoff(int days) {
        return LocalDate.now().minusDays(days - 1L).atStartOfDay();
    }

    private void validateDays(int days) {
        if (days < 1 || days > 365) {
            log.warn("Invalid dashboard days parameter received: {}", days);
            throw new IllegalArgumentException("days must be between 1 and 365.");
        }
    }
}
