package com.example.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository, 
                        InventoryService inventoryService,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.notificationService = notificationService;
    }

    public Order createOrder(String customerId, List<OrderItem> items) {
        if (customerId == null || customerId.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        for (OrderItem item : items) {
            if (!inventoryService.isInStock(item.getProductId(), item.getQuantity())) {
                throw new InsufficientStockException(item.getProductId());
            }
        }

        BigDecimal total = calculateTotal(items);
        Order order = new Order(customerId, items, total);
        
        Order savedOrder = orderRepository.save(order);
        
        for (OrderItem item : items) {
            inventoryService.reserve(item.getProductId(), item.getQuantity());
        }
        
        notificationService.sendOrderConfirmation(customerId, savedOrder.getId());
        
        return savedOrder;
    }

    public Optional<Order> findOrder(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return Optional.empty();
        }
        return orderRepository.findById(orderId);
    }

    public void cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel shipped order");
        }

        for (OrderItem item : order.getItems()) {
            inventoryService.release(item.getProductId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        
        notificationService.sendCancellationConfirmation(order.getCustomerId(), orderId);
    }

    public BigDecimal calculateTotal(List<OrderItem> items) {
        return items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Order applyDiscount(String orderId, BigDecimal discountPercentage) {
        if (discountPercentage.compareTo(BigDecimal.ZERO) < 0 
                || discountPercentage.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("Discount must be between 0 and 100");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        BigDecimal multiplier = BigDecimal.valueOf(100)
                .subtract(discountPercentage)
                .divide(BigDecimal.valueOf(100));
        
        BigDecimal newTotal = order.getTotal().multiply(multiplier);
        order.setTotal(newTotal);
        
        return orderRepository.save(order);
    }
}