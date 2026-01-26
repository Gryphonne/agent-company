package com.example.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class Order {
    private final String id;
    private final String customerId;
    private final List<OrderItem> items;
    private BigDecimal total;
    private OrderStatus status;

    public Order(String customerId, List<OrderItem> items, BigDecimal total) {
        this.id = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.items = items;
        this.total = total;
        this.status = OrderStatus.PENDING;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
