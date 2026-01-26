package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private NotificationService notificationService;

    private OrderService serviceUnderTest;

    @BeforeEach
    void setUp() {
        serviceUnderTest = new OrderService(orderRepository, inventoryService, notificationService);
    }

    @Test
    void should_createOrder_when_validCustomerIdAndItems() {
        // Arrange
        var customerId = "customer-123";
        var item = new OrderItem("product-1", 2, new BigDecimal("10.00"));
        var items = List.of(item);
        when(inventoryService.isInStock("product-1", 2)).thenReturn(true);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = serviceUnderTest.createOrder(customerId, items);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("20.00"));
        verify(inventoryService).reserve("product-1", 2);
        verify(notificationService).sendOrderConfirmation(eq(customerId), anyString());
    }

    @Test
    void should_throwException_when_customerIdIsNull() {
        // Arrange
        var items = List.of(new OrderItem("product-1", 1, new BigDecimal("10.00")));

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.createOrder(null, items))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Customer ID cannot be empty");
    }

    @Test
    void should_throwException_when_customerIdIsBlank() {
        // Arrange
        var items = List.of(new OrderItem("product-1", 1, new BigDecimal("10.00")));

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.createOrder("   ", items))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Customer ID cannot be empty");
    }

    @Test
    void should_throwException_when_itemsIsNull() {
        // Arrange
        var customerId = "customer-123";

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.createOrder(customerId, null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Order must contain at least one item");
    }

    @Test
    void should_throwException_when_itemsIsEmpty() {
        // Arrange
        var customerId = "customer-123";
        List<OrderItem> emptyItems = Collections.emptyList();

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.createOrder(customerId, emptyItems))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Order must contain at least one item");
    }

    @Test
    void should_throwInsufficientStockException_when_itemNotInStock() {
        // Arrange
        var customerId = "customer-123";
        var item = new OrderItem("product-1", 10, new BigDecimal("10.00"));
        var items = List.of(item);
        when(inventoryService.isInStock("product-1", 10)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.createOrder(customerId, items))
            .isInstanceOf(InsufficientStockException.class)
            .hasMessageContaining("product-1");
    }

    @Test
    void should_returnOrder_when_orderExists() {
        // Arrange
        var orderId = "order-123";
        var order = new Order("customer-1", List.of(new OrderItem("p1", 1, BigDecimal.TEN)), BigDecimal.TEN);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        var result = serviceUnderTest.findOrder(orderId);

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getCustomerId()).isEqualTo("customer-1");
    }

    @Test
    void should_returnEmptyOptional_when_orderNotFound() {
        // Arrange
        var orderId = "non-existent-order";
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act
        var result = serviceUnderTest.findOrder(orderId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void should_returnEmptyOptional_when_orderIdIsNull() {
        // Arrange & Act
        var result = serviceUnderTest.findOrder(null);

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(orderRepository);
    }

    @Test
    void should_returnEmptyOptional_when_orderIdIsBlank() {
        // Arrange & Act
        var result = serviceUnderTest.findOrder("   ");

        // Assert
        assertThat(result).isEmpty();
        verifyNoInteractions(orderRepository);
    }

    @Test
    void should_cancelOrder_when_orderIsPending() {
        // Arrange
        var orderId = "order-123";
        var item = new OrderItem("product-1", 2, new BigDecimal("10.00"));
        var order = new Order("customer-1", List.of(item), new BigDecimal("20.00"));
        order.setStatus(OrderStatus.PENDING);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act
        serviceUnderTest.cancelOrder(orderId);

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(inventoryService).release("product-1", 2);
        verify(orderRepository).save(order);
        verify(notificationService).sendCancellationConfirmation("customer-1", orderId);
    }

    @Test
    void should_throwOrderNotFoundException_when_cancellingNonExistentOrder() {
        // Arrange
        var orderId = "non-existent-order";
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.cancelOrder(orderId))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining(orderId);
    }

    @Test
    void should_throwException_when_cancellingShippedOrder() {
        // Arrange
        var orderId = "order-123";
        var order = new Order("customer-1", List.of(new OrderItem("p1", 1, BigDecimal.TEN)), BigDecimal.TEN);
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.cancelOrder(orderId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot cancel shipped order");
    }

    @Test
    void should_calculateTotal_when_multipleItems() {
        // Arrange
        var item1 = new OrderItem("product-1", 2, new BigDecimal("10.00"));
        var item2 = new OrderItem("product-2", 3, new BigDecimal("5.00"));
        var items = List.of(item1, item2);

        // Act
        var result = serviceUnderTest.calculateTotal(items);

        // Assert
        assertThat(result).isEqualByComparingTo(new BigDecimal("35.00"));
    }

    @Test
    void should_calculateTotal_when_singleItem() {
        // Arrange
        var item = new OrderItem("product-1", 1, new BigDecimal("25.50"));
        var items = List.of(item);

        // Act
        var result = serviceUnderTest.calculateTotal(items);

        // Assert
        assertThat(result).isEqualByComparingTo(new BigDecimal("25.50"));
    }

    @Test
    void should_returnZero_when_emptyItemList() {
        // Arrange
        List<OrderItem> emptyItems = Collections.emptyList();

        // Act
        var result = serviceUnderTest.calculateTotal(emptyItems);

        // Assert
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void should_applyDiscount_when_validDiscountPercentage() {
        // Arrange
        var orderId = "order-123";
        var order = new Order("customer-1", List.of(new OrderItem("p1", 1, BigDecimal.TEN)), new BigDecimal("100.00"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = serviceUnderTest.applyDiscount(orderId, new BigDecimal("20"));

        // Assert
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("80.00"));
    }

    @Test
    void should_throwException_when_discountIsNegative() {
        // Arrange
        var orderId = "order-123";
        var negativeDiscount = new BigDecimal("-10");

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.applyDiscount(orderId, negativeDiscount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Discount must be between 0 and 100");
    }

    @Test
    void should_throwException_when_discountExceeds100() {
        // Arrange
        var orderId = "order-123";
        var excessiveDiscount = new BigDecimal("101");

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.applyDiscount(orderId, excessiveDiscount))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Discount must be between 0 and 100");
    }

    @Test
    void should_keepSameTotal_when_zeroPercentDiscount() {
        // Arrange
        var orderId = "order-123";
        var order = new Order("customer-1", List.of(new OrderItem("p1", 1, BigDecimal.TEN)), new BigDecimal("100.00"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = serviceUnderTest.applyDiscount(orderId, BigDecimal.ZERO);

        // Assert
        assertThat(result.getTotal()).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    void should_returnZeroTotal_when_fullDiscount() {
        // Arrange
        var orderId = "order-123";
        var order = new Order("customer-1", List.of(new OrderItem("p1", 1, BigDecimal.TEN)), new BigDecimal("100.00"));
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var result = serviceUnderTest.applyDiscount(orderId, new BigDecimal("100"));

        // Assert
        assertThat(result.getTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void should_throwOrderNotFoundException_when_applyingDiscountToNonExistentOrder() {
        // Arrange
        var orderId = "non-existent-order";
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> serviceUnderTest.applyDiscount(orderId, new BigDecimal("10")))
            .isInstanceOf(OrderNotFoundException.class)
            .hasMessageContaining(orderId);
    }
}
