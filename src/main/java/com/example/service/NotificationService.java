package com.example.service;

public interface NotificationService {
    void sendOrderConfirmation(String customerId, String orderId);
    void sendCancellationConfirmation(String customerId, String orderId);
}
