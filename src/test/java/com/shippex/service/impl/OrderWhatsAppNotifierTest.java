package com.shippex.service.impl;

import com.shippex.constants.AccountStatus;
import com.shippex.dto.notification.NotificationRequest;
import com.shippex.model.AdminUser;
import com.shippex.model.AppUser;
import com.shippex.model.DeliveryDestination;
import com.shippex.model.Order;
import com.shippex.model.OrderItem;
import com.shippex.model.OrderStatus;
import com.shippex.repository.AdminUserRepository;
import com.shippex.repository.AppUserRepository;
import com.shippex.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderWhatsAppNotifierTest {
    @Mock NotificationService notificationService;
    @Mock AppUserRepository appUserRepository;
    @Mock AdminUserRepository adminUserRepository;
    @InjectMocks OrderWhatsAppNotifier notifier;

    private Order order;
    private AppUser customer;
    private AdminUser admin;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId("order-id");
        order.setOrderNumber("ORD-123");
        order.setUserId("user-id");
        order.setStatus(OrderStatus.PLACED);
        order.setCurrency("USD");
        order.setTotalAmount(new BigDecimal("42.50"));
        order.setPaymentMethod("CARD");
        order.setEstimatedDeliveryDateTime(LocalDateTime.of(2026, 9, 28, 15, 0));
        order.setDeliveryInstructions("Call on arrival");
        order.setOrderInstructions("Keep dry");
        DeliveryDestination destination = new DeliveryDestination();
        destination.setShipName("MV Example");
        destination.setPortName("Mumbai");
        destination.setBerthNumber("B-4");
        order.setDeliveryDestination(destination);
        OrderItem item = new OrderItem();
        item.setName("Deck gloves");
        item.setSku("DG-1");
        item.setQuantity(2);
        item.setCurrency("USD");
        item.setSubtotal(new BigDecimal("42.50"));
        order.setItems(List.of(item));

        customer = new AppUser();
        customer.setId("user-id");
        customer.setName("Sam");
        customer.setWhatsappContactNo("+15550000001");
        admin = new AdminUser();
        admin.setId("admin-id");
        admin.setWhatsappContactNo("+15550000002");
    }

    @Test
    void orderPlaced_sendsItemizedFriendlyMessagesToCustomerAndActiveAdmins() {
        when(appUserRepository.findById("user-id")).thenReturn(Optional.of(customer));
        when(adminUserRepository.findByAccountStatus(AccountStatus.ACTIVE)).thenReturn(List.of(admin));

        notifier.orderPlaced(order);

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService, times(2)).sendTextMessage(captor.capture());
        List<NotificationRequest> requests = captor.getAllValues();
        assertEquals("+15550000001", requests.get(0).getRecipient());
        assertTrue(requests.get(0).getMessage().contains("Sam"));
        assertTrue(requests.get(0).getMessage().contains("🎉"));
        assertTrue(requests.get(0).getMessage().contains("Deck gloves (DG-1) × 2"));
        assertTrue(requests.get(0).getMessage().contains("Team Shippex"));
        assertEquals("+15550000002", requests.get(1).getRecipient());
        assertTrue(requests.get(1).getMessage().contains("New order placed"));
        assertTrue(requests.get(1).getMessage().contains("Deck gloves (DG-1) × 2"));
        assertTrue(requests.get(1).getMessage().contains("USD 42.50"));
    }

    @Test
    void statusChanged_sendsDetailedUpdatedStatusToBothAudiences() {
        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        when(appUserRepository.findById("user-id")).thenReturn(Optional.of(customer));
        when(adminUserRepository.findByAccountStatus(AccountStatus.ACTIVE)).thenReturn(List.of(admin));

        notifier.statusChanged(order);

        ArgumentCaptor<NotificationRequest> captor = ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationService, times(2)).sendTextMessage(captor.capture());
        for (NotificationRequest request : captor.getAllValues()) {
            assertTrue(request.getMessage().contains("Out for delivery"));
            assertTrue(request.getMessage().contains("ORD-123"));
            assertTrue(request.getMessage().contains("Deck gloves"));
            assertTrue(request.getMessage().contains("Mumbai, B-4"));
            assertTrue(request.getMessage().contains("Call on arrival"));
            assertTrue(request.getMessage().contains("Keep dry"));
            assertTrue(request.getMessage().contains("CARD"));
        }
    }

    @Test
    void skipsRecipientsWithoutPhoneAndExcludesInactiveAdmins() {
        customer.setWhatsappContactNo(" ");
        AdminUser noPhone = new AdminUser();
        noPhone.setId("no-phone");
        when(appUserRepository.findById("user-id")).thenReturn(Optional.of(customer));
        when(adminUserRepository.findByAccountStatus(AccountStatus.ACTIVE)).thenReturn(List.of(noPhone));

        notifier.orderPlaced(order);

        verifyNoInteractions(notificationService);
        verify(adminUserRepository).findByAccountStatus(AccountStatus.ACTIVE);
    }

    @Test
    void missingCustomerStillNotifiesAdmins() {
        when(appUserRepository.findById("user-id")).thenReturn(Optional.empty());
        when(adminUserRepository.findByAccountStatus(AccountStatus.ACTIVE)).thenReturn(List.of(admin));

        notifier.statusChanged(order);

        verify(notificationService).sendTextMessage(argThat(request ->
                request.getRecipient().equals("+15550000002") && request.getMessage().contains("ORD-123")));
    }

    @Test
    void whatsappFailureIsIsolatedPerRecipient() {
        when(appUserRepository.findById("user-id")).thenReturn(Optional.of(customer));
        when(adminUserRepository.findByAccountStatus(AccountStatus.ACTIVE)).thenReturn(List.of(admin));
        doThrow(new IllegalStateException("provider down")).when(notificationService).sendTextMessage(any());

        assertDoesNotThrow(() -> notifier.orderPlaced(order));
        verify(notificationService, times(2)).sendTextMessage(any());
    }

    @Test
    void adminLookupFailureDoesNotFailNotificationTask() {
        when(appUserRepository.findById("user-id")).thenReturn(Optional.of(customer));
        when(adminUserRepository.findByAccountStatus(AccountStatus.ACTIVE)).thenThrow(new IllegalStateException("database down"));

        assertDoesNotThrow(() -> notifier.statusChanged(order));
        verify(notificationService).sendTextMessage(argThat(request -> request.getRecipient().equals("+15550000001")));
    }
}
