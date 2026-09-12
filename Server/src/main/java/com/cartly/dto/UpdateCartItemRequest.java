package com.cartly.dto;

public class UpdateCartItemRequest {

    private Integer quantity;

    public UpdateCartItemRequest() {
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}