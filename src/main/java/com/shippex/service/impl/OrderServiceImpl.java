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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    @Override
    public Order placeOrder(String userId, PlaceOrderRequest request) {
        List<OrderItem> items = new ArrayList<>();
        List<Product> productsToUpdate = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        String currency = null;

        for (OrderItemRequest requestedItem : request.getItems()) {
            Product product = productRepository.findById(requestedItem.getProductId())
                    .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + requestedItem.getProductId()));

            if (!Boolean.TRUE.equals(product.getActive())) {
                throw new IllegalArgumentException("Product is not available: " + product.getName());
            }
            if (product.getStock() == null || product.getStock() < requestedItem.getQuantity()) {
                throw new InsufficientStockException("Insufficient stock for product: " + product.getName());
            }
            if (currency != null && !currency.equals(product.getCurrency())) {
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
        order.setEstimatedDeliveryDate(request.getEstimatedDeliveryDate());
        order.setEstimatedDeliveryTime(request.getEstimatedDeliveryTime());
        order.setDeliveryInstructions(request.getDeliveryInstructions());
        order.setOrderInstructions(request.getOrderInstructions());
        order.setPaymentMethod(request.getPaymentMethod());
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getOrdersForUser(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Override
    public Order cancelOrder(String orderId, String userId) {
        Order order = findOrder(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new OrderNotFoundException("Order not found with id: " + orderId);
        }
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new InvalidOrderStatusException("Only placed or confirmed orders can be cancelled.");
        }
        restock(order);
        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @Override
    public Order updateStatus(String orderId, UpdateOrderStatusRequest request) {
        Order order = findOrder(orderId);
        if (order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException("A delivered or cancelled order cannot be updated.");
        }
        if (request.getStatus() == OrderStatus.CANCELLED) {
            restock(order);
        }
        order.setStatus(request.getStatus());
        return orderRepository.save(order);
    }

    private Order findOrder(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + orderId));
    }

    private void restock(Order order) {
        List<Product> products = new ArrayList<>();
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.setStock((product.getStock() == null ? 0 : product.getStock()) + item.getQuantity());
                products.add(product);
            });
        }
        productRepository.saveAll(products);
    }
}
