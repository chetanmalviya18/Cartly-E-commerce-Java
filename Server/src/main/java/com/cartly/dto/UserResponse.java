package com.cartly.dto;

import com.cartly.entity.UserRole;

public class UserResponse {
	private Long id;
	private String name;
	private String email;
	private UserRole role;
	
	public UserResponse() {
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
	    this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public UserRole getRole() {
		return role;
	}
	public void setRole(UserRole role) {
		this.role = role;
	}
}
