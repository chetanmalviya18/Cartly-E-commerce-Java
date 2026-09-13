package com.cartly.dto;

import com.cartly.entity.Order;
import com.cartly.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class OrderResponse {

    private Long id;
    private String status;
    private BigDecimal totalAmount;

    private String shippingName;
    private String shippingPhone;
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingPincode;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;

    public OrderResponse() {
    }

    public OrderResponse(
            Order order,
            List<OrderItem> orderItems) {

        this.id = order.getId();
        this.status = order.getStatus().name();
        this.totalAmount = order.getTotalAmount();

        this.shippingName = order.getShippingName();
        this.shippingPhone = order.getShippingPhone();
        this.shippingAddress = order.getShippingAddress();
        this.shippingCity = order.getShippingCity();
        this.shippingState = order.getShippingState();
        this.shippingPincode = order.getShippingPincode();

        this.createdAt = order.getCreatedAt();

        this.items = orderItems.stream()
                .map(OrderItemResponse::new)
                .toList();
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getShippingName() {
        return shippingName;
    }

    public String getShippingPhone() {
        return shippingPhone;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public String getShippingCity() {
        return shippingCity;
    }

    public String getShippingState() {
        return shippingState;
    }

    public String getShippingPincode() {
        return shippingPincode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }
}