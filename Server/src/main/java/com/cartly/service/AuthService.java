package com.cartly.service;

import com.cartly.dao.UserDAO;
import com.cartly.dto.LoginRequest;
import com.cartly.dto.RegisterRequest;
import com.cartly.dto.UserResponse;
import com.cartly.entity.User;
import com.cartly.entity.UserRole;
import com.cartly.util.PasswordUtil;

public class AuthService {
	
	private final UserDAO userDAO;
	
	public AuthService(UserDAO userDAO) {
		this.userDAO = userDAO;
	}
	
	public UserResponse register(RegisterRequest request) {
		String email = request.getEmail().trim().toLowerCase();
		
		User existingUser = userDAO.findByEmail(email);
		
		if(existingUser != null) {
			throw new IllegalArgumentException("Email already registered");
		}
		
		User user = new User();
		
		user.setName(request.getName().trim());
		user.setEmail(email);
		user.setPassword(PasswordUtil.hashPassword(request.getPassword()));
		
		user.setRole(UserRole.CUSTOMER);
		user.setActive(true);
		
		userDAO.save(user);
		
		UserResponse response = new UserResponse();
		
		response.setId(user.getId());
		response.setName(user.getName());
		response.setEmail(user.getEmail());
		response.setRole(user.getRole());
		
		return response;
	}
	
	public UserResponse login(LoginRequest request) {
		String email = request.getEmail().trim().toLowerCase();
		
		User user = userDAO.findByEmail(email);
		
		if (user == null) {
	        throw new IllegalArgumentException("Invalid email or password");
	    }
		
		if(!user.isActive()) {
			throw new IllegalArgumentException("User account is inactive");
		}
		
		boolean passwordMatches = 
				PasswordUtil.verifyPassword(request.getPassword(), user.getPassword());
		
		if (!passwordMatches) {
	        throw new IllegalArgumentException("Invalid email or password");
	    }
		
		UserResponse response = new UserResponse();

	    response.setId(user.getId());
	    response.setName(user.getName());
	    response.setEmail(user.getEmail());
	    response.setRole(user.getRole());

	    return response;
	}
}
