package com.shippex.service.impl;

import com.shippex.constants.AccountStatus;
import com.shippex.dto.notification.NotificationRequest;
import com.shippex.model.AdminUser;
import com.shippex.model.AppUser;
import com.shippex.model.DeliveryDestination;
import com.shippex.model.Order;
import com.shippex.model.OrderItem;
import com.shippex.repository.AdminUserRepository;
import com.shippex.repository.AppUserRepository;
import com.shippex.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderWhatsAppNotifier {
    private static final String TEAM_SIGNATURE = "\n\nTeam Shippex";

    private final NotificationService notificationService;
    private final AppUserRepository appUserRepository;
    private final AdminUserRepository adminUserRepository;

    @Async("whatsappNotificationExecutor")
    public void orderPlaced(Order order) {
        AppUser customer = findCustomer(order);
        notifyCustomer(order, customer, customerPlacementMessage(order, customer));
        notifyAdmins(order, customer, adminPlacementMessage(order, customer));
    }

    @Async("whatsappNotificationExecutor")
    public void statusChanged(Order order) {
        AppUser customer = findCustomer(order);
        String message = statusMessage(order, customer);
        notifyCustomer(order, customer, message);
        notifyAdmins(order, customer, message);
    }

    private AppUser findCustomer(Order order) {
        try {
            return appUserRepository.findById(order.getUserId()).orElse(null);
        } catch (RuntimeException exception) {
            log.error("Unable to load customer for WhatsApp notification on order {}", order.getOrderNumber(), exception);
            return null;
        }
    }

    private void notifyCustomer(Order order, AppUser customer, String message) {
        if (customer == null) {
            log.warn("Skipping customer WhatsApp notification for order {}: customer is unavailable", order.getOrderNumber());
            return;
        }
        if (!hasPhone(customer.getWhatsappContactNo())) {
            log.warn("Skipping customer WhatsApp notification for order {}: no contact number", order.getOrderNumber());
            return;
        }
        send(customer.getWhatsappContactNo(), message, "customer", order);
    }

    private void notifyAdmins(Order order, AppUser customer, String message) {
        try {
            List<AdminUser> admins = adminUserRepository.findByAccountStatus(AccountStatus.ACTIVE);
            for (AdminUser admin : admins) {
                if (hasPhone(admin.getWhatsappContactNo())) {
                    send(admin.getWhatsappContactNo(), message, "admin", order);
                } else {
                    log.warn("Skipping admin WhatsApp notification: active admin {} has no contact number", admin.getId());
                }
            }
        } catch (RuntimeException exception) {
            log.error("Unable to prepare admin WhatsApp notifications for order {}", order.getOrderNumber(), exception);
        }
    }

    private String customerPlacementMessage(Order order, AppUser customer) {
        return "🎉 Thanks for your order *" + customerName(customer) + "*!\n"
                + "Your Shippex order *" + order.getOrderNumber() + "* current status is *Placed*.\n\n"
                + "📦 *Items*\n" + itemLinesCustomer(order)
                + "\n💰 *Total:* " + money(order) +"\n"+ deliveryDetails(order)
                + "\n\nWe’ll keep you updated as your order moves along." + TEAM_SIGNATURE;
    }

    private String adminPlacementMessage(Order order, AppUser customer) {
        return "🛒 *New order placed*\n"
                + "Order: *" + order.getOrderNumber() + "*\n"
                + "Customer: " + customerName(customer) + customerPhone(customer) + "\n\n"
                + "📦 *Items*\n" + itemLinesAdmin(order)
                + "\n💰 *Total:* " + money(order) + "\n" + deliveryDetails(order)
                + "\n*\uD83D\uDCB3 Payment:* " + display(order.getPaymentMethod().replace("_", " "), "Not specified");
    }

    private String statusMessage(Order order, AppUser customer) {
        String status = order.getStatus().name().toLowerCase(Locale.ROOT).replace('_', ' ');
        return "📣 *Order update" + (customer == null ? "" : " for " + customerName(customer)) + "*\n"
                + "Order: *" + order.getOrderNumber() + "*\n"
                + "Status: *" + capitalize(status) + "*\n\n"
                + "📦 *Items*\n" + itemLinesCustomer(order)
                + "\n💰 *Total:* " + money(order) + "\n" + deliveryDetails(order)
                + "\n*\uD83D\uDCB3 Payment:* " + display(order.getPaymentMethod().replace("_", " "), "Not specified") + "\n"
                + (hasText(order.getOrderInstructions()) ? "\n*Order notes:* " + order.getOrderInstructions() : "")
                + (hasText(order.getDeliveryInstructions()) ? "\n*Delivery notes:* " + order.getDeliveryInstructions() : "")
                + (customer == null ? "" : customerPhone(customer))
                + TEAM_SIGNATURE;
    }

    private String itemLinesCustomer(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return "• Item details unavailable";
        }
        return order.getItems().stream()
                .map(item -> "• " + display(item.getName(), "Item")
                        + (hasText(item.getSku()) ? " (" + item.getSku() + ")" : "")
                        + " × " + Objects.toString(item.getQuantity(), "0")
//                        + " — " + display(item.getCurrency(), order.getCurrency()) + " "
//                        + Objects.toString(item.getSubtotal(), "0")
                )
                .collect(Collectors.joining("\n"));
    }

    private String itemLinesAdmin(Order order) {
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return "• Item details unavailable";
        }
        return order.getItems().stream()
                .map(item -> "• " + display(item.getName(), "Item")
                        + (hasText(item.getSku()) ? " (" + item.getSku() + ")" : "")
                                + " × " + Objects.toString(item.getQuantity(), "0")
                        + " — " + display(item.getCurrency(), order.getCurrency()) + " "
                        + Objects.toString(item.getSubtotal(), "0")
                )
                .collect(Collectors.joining("\n"));
    }

    private String deliveryDetails(Order order) {
        DeliveryDestination destination = order.getDeliveryDestination();
        StringBuilder details = new StringBuilder();
        if (destination != null) {
            String location = Stream.of(destination.getPortName(), destination.getBerthNumber())
                    .filter(OrderWhatsAppNotifier::hasText).collect(Collectors.joining(", "));
            if (hasText(destination.getShipName())) details.append("\n🚢 *Ship:* ").append(destination.getShipName());
            if (!location.isBlank()) details.append("\n📍 *Delivery:* ").append(location);
            if (hasText(destination.getImoNumber())) details.append("\nℹ️ *IMO:* ").append(destination.getImoNumber());
        }
        if (order.getEstimatedDeliveryDateTime() != null) {
            details.append("\n🕒 *Estimated delivery:* ").append(order.getEstimatedDeliveryDateTime().toString().replace("T", " "));
        }
        return details.toString();
    }

    private void send(String phone, String message, String recipientType, Order order) {
        try {
            notificationService.sendTextMessage(new NotificationRequest(phone, message));
        } catch (RuntimeException exception) {
            log.error("WhatsApp notification failed for {} recipient on order {}", recipientType, order.getOrderNumber(), exception);
        }
    }

    private String customerName(AppUser customer) {
        return customer == null ? "" : display(customer.getName(), "");
    }

    private String customerPhone(AppUser customer) {
        return customer == null || !hasPhone(customer.getWhatsappContactNo())
                ? "" : " (" + customer.getWhatsappContactNo() + ")";
    }

    private String money(Order order) {
        return Objects.toString(order.getTotalAmount(), "0") +  " " + display(order.getCurrency(), "");
    }

    private String display(String value, String fallback) {
        return hasText(value) ? value : fallback;
    }

    private String capitalize(String value) {
        return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private boolean hasPhone(String value) {
        return hasText(value);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
