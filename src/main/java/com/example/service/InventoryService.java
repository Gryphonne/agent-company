package com.example.service;

public interface InventoryService {
    boolean isInStock(String productId, int quantity);
    void reserve(String productId, int quantity);
    void release(String productId, int quantity);
}
