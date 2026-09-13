package com.cartly.dto;

import com.cartly.entity.OrderItem;

import java.math.BigDecimal;

public class OrderItemResponse {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    public OrderItemResponse() {
    }

    public OrderItemResponse(OrderItem item) {
        this.productId = item.getProduct().getId();
        this.productName = item.getProduct().getName();
        this.quantity = item.getQuantity();
        this.price = item.getPrice();

        this.subtotal = item.getPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    public Long getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}