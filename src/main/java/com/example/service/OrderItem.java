package com.example.service;

import java.math.BigDecimal;

public class OrderItem {
    private final String productId;
    private final int quantity;
    private final BigDecimal price;

    public OrderItem(String productId, int quantity, BigDecimal price) {
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }
}
