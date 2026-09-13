package com.cartly.dto;

import com.cartly.entity.Payment;
import com.cartly.entity.PaymentMethod;
import com.cartly.entity.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private Long orderId;
    private PaymentMethod method;
    private PaymentStatus status;
    private BigDecimal amount;
    private String transactionReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public PaymentResponse() {
    }

    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.orderId = payment.getOrder().getId();
        this.method = payment.getMethod();
        this.status = payment.getStatus();
        this.amount = payment.getAmount();
        this.transactionReference = payment.getTransactionReference();
        this.createdAt = payment.getCreatedAt();
        this.updatedAt = payment.getUpdatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public PaymentMethod getMethod() {
        return method;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}