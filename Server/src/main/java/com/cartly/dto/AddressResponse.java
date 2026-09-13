package com.cartly.dto;

import com.cartly.entity.Address;

public class AddressResponse {

    private Long id;
    private String fullName;
    private String phone;
    private String addressLine;
    private String city;
    private String state;
    private String pincode;

    public AddressResponse() {
    }

    public AddressResponse(Address address) {
        this.id = address.getId();
        this.fullName = address.getFullName();
        this.phone = address.getPhone();
        this.addressLine = address.getAddressLine();
        this.city = address.getCity();
        this.state = address.getState();
        this.pincode = address.getPincode();
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddressLine() {
        return addressLine;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getPincode() {
        return pincode;
    }
}