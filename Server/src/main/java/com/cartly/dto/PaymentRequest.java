package com.cartly.dto;

import com.cartly.entity.PaymentMethod;

public class PaymentRequest {

    private Long orderId;
    private PaymentMethod method;

    public PaymentRequest() {
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public void setMethod(PaymentMethod method) {
        this.method = method;
    }
}