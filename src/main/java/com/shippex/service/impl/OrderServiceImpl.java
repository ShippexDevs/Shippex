package com.shippex.service.impl;

import com.shippex.dto.order.OrderItemRequest;
import com.shippex.dto.order.PlaceOrderRequest;
import com.shippex.dto.order.UpdateOrderStatusRequest;
import com.shippex.exception.InsufficientStockException;
import com.shippex.exception.InvalidOrderStatusException;
import com.shippex.exception.OrderNotFoundException;
import com.shippex.exception.ProductNotFoundException;
import com.shippex.model.Order;
import com.shippex.model.DeliveryDestination;
import com.shippex.model.OrderItem;
import com.shippex.model.OrderStatus;
import com.shippex.model.Product;
import com.shippex.repository.OrderRepository;
import com.shippex.repository.ProductRepository;
import com.shippex.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public Order placeOrder(String userId, PlaceOrderRequest request) {
        log.info("Placing order for userId={} with itemCount={}", userId, request.getItems().size());
        List<OrderItem> items = new ArrayList<>();
        List<Product> productsToUpdate = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        String currency = null;

        for (OrderItemRequest requestedItem : request.getItems()) {
            log.debug("Processing order item for userId={}, productId={}, quantity={}",
                    userId, requestedItem.getProductId(), requestedItem.getQuantity());
            Product product = productRepository.findById(requestedItem.getProductId())
                    .orElseThrow(() -> {
                        log.warn("Order placement failed: productId={} was not found for userId={}",
                                requestedItem.getProductId(), userId);
                        return new ProductNotFoundException("Product not found with id: " + requestedItem.getProductId());
                    });

            if (!Boolean.TRUE.equals(product.getActive())) {
                log.warn("Order placement rejected: productId={} is inactive", product.getId());
                throw new IllegalArgumentException("Product is not available: " + product.getName());
            }
            if (product.getStock() == null || product.getStock() < requestedItem.getQuantity()) {
                log.warn("Order placement rejected: insufficient stock for productId={}, requestedQuantity={}, availableStock={}",
                        product.getId(), requestedItem.getQuantity(), product.getStock());
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
            if (currency != null && !currency.equals(product.getCurrency())) {
                log.warn("Order placement rejected: mixed currencies userId={}, expectedCurrency={}, productCurrency={}",
                        userId, currency, product.getCurrency());
                throw new IllegalArgumentException("All ordered products must use the same currency.");
            }

            BigDecimal subtotal = product.getCurrentPrice().multiply(BigDecimal.valueOf(requestedItem.getQuantity()));
            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setSku(product.getSku());
            item.setName(product.getName());
            item.setImage(product.getImages() == null || product.getImages().isEmpty() ? null : product.getImages().getFirst());
            item.setUnit(product.getUnit());
            item.setCurrency(product.getCurrency());
            item.setUnitPrice(product.getCurrentPrice());
            item.setQuantity(requestedItem.getQuantity());
            item.setSubtotal(subtotal);
            items.add(item);
            productsToUpdate.add(product);
            total = total.add(subtotal);
            currency = product.getCurrency();
            product.setStock(product.getStock() - requestedItem.getQuantity());
        }

        productRepository.saveAll(productsToUpdate);
        log.debug("Reserved stock for {} product entries for userId={}", productsToUpdate.size(), userId);

        Order order = new Order();
        order.setOrderNumber("ORD-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase());
        order.setUserId(userId);
        order.setItems(items);
        order.setCurrency(currency);
        order.setTotalAmount(total);
        order.setStatus(OrderStatus.PLACED);
        DeliveryDestination destination = new DeliveryDestination();
        destination.setShipName(request.getDeliveryDestination().getShipName());
        destination.setImoNumber(request.getDeliveryDestination().getImoNumber());
        destination.setBerthNumber(request.getDeliveryDestination().getBerthNumber());
        destination.setPortName(request.getDeliveryDestination().getPortName());
        order.setDeliveryDestination(destination);
        order.setEstimatedDeliveryDateTime(request.getEstimatedDeliveryDateTime());
        order.setDeliveryInstructions(request.getDeliveryInstructions());
        order.setOrderInstructions(request.getOrderInstructions());
        order.setPaymentMethod(request.getPaymentMethod());
        Order savedOrder = orderRepository.save(order);
        log.info("Order placed successfully: orderId={}, orderNumber={}, userId={}, total={}, currency={}",
                savedOrder.getId(), savedOrder.getOrderNumber(), userId, savedOrder.getTotalAmount(), savedOrder.getCurrency());
        return savedOrder;
    }

    @Override
    public List<Order> getOrdersForUser(String userId) {
        log.debug("Fetching orders for userId={}", userId);
        List<Order> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        log.debug("Found {} orders for userId={}", orders.size(), userId);
        return orders;
    }

    @Override
    public Order cancelOrder(String orderId, String userId) {
        log.info("Cancelling orderId={} for userId={}", orderId, userId);
        Order order = findOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            log.warn("Order cancellation denied: orderId={} does not belong to userId={}", orderId, userId);
            throw new OrderNotFoundException("Order not found with id: " + orderId);
        }
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            log.warn("Order cancellation rejected: orderId={} has status={}", orderId, order.getStatus());
            throw new InvalidOrderStatusException("Only placed or confirmed orders can be cancelled.");
        }
        restock(order);
        order.setStatus(OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);
        log.info("Order cancelled successfully: orderId={}, userId={}", orderId, userId);
        return cancelledOrder;
    }

    @Override
    public List<Order> getAllOrders() {
        log.debug("Fetching all orders for administration");
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Order updateStatus(String orderId, UpdateOrderStatusRequest request) {
        Order order = findOrder(orderId);
        log.info("Updating order status: orderId={}, fromStatus={}, toStatus={}",
                orderId, order.getStatus(), request.getStatus());
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            log.warn("Order status update rejected: orderId={} is already terminal with status={}", orderId, order.getStatus());
            throw new InvalidOrderStatusException("A delivered or cancelled order cannot be updated.");
        }
        if (request.getStatus() == OrderStatus.CANCELLED) {
            restock(order);
        }
        order.setStatus(request.getStatus());
        Order updatedOrder = orderRepository.save(order);
        log.info("Order status updated successfully: orderId={}, status={}", orderId, updatedOrder.getStatus());
        return updatedOrder;
    }

    private Order findOrder(String orderId) {
        log.debug("Looking up orderId={}", orderId);
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.warn("Order not found: orderId={}", orderId);
                    return new OrderNotFoundException("Order not found with id: " + orderId);
                });
    }

    private void restock(Order order) {
        log.debug("Restocking {} item entries for orderId={}", order.getItems().size(), order.getId());
        List<Product> products = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId()).ifPresentOrElse(product -> {
                        product.setStock((product.getStock() == null ? 0 : product.getStock()) + item.getQuantity());
                        products.add(product);
                    }, () -> log.warn("Unable to restock missing productId={} for orderId={}", item.getProductId(), order.getId()));
        }
        productRepository.saveAll(products);
        log.debug("Restocked {} products for orderId={}", products.size(), order.getId());
    }
}
