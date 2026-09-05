package com.cartly.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import com.cartly.dao.UserDAO;
import com.cartly.dto.LoginRequest;
import com.cartly.dto.RegisterRequest;
import com.cartly.dto.UserResponse;
import com.cartly.service.AuthService;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	private final ObjectMapper objectMapper = new ObjectMapper();
	private final AuthService authService = new AuthService(new UserDAO());
	

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String path = request.getPathInfo();
		
		if("/register".equals(path)) {
			register(request, response);
		} else if("/login".equals(path)) {
			login(request, response);
		} else if ("/logout".equals(path)) {
            logout(request, response);
        } else {
        	response.sendError(
        			HttpServletResponse.SC_NOT_FOUND,
        			"Endpoint not found"
        	);
        }
	}
	
	private void register(HttpServletRequest request, HttpServletResponse response) throws IOException {
		RegisterRequest registerRequest = objectMapper.readValue(request.getReader(), RegisterRequest.class);
		
		UserResponse userResponse = authService.register(registerRequest);
		
		response.setStatus(HttpServletResponse.SC_CREATED);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		objectMapper.writeValue(response.getWriter(), userResponse);
		
	}
	
	private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
		LoginRequest loginRequest = objectMapper.readValue(request.getReader(), LoginRequest.class);
		
		UserResponse userResponse = authService.login(loginRequest);
		
		var session = request.getSession(true);
		session.setAttribute("userId", userResponse.getId());
		session.setAttribute("role", userResponse.getRole());
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		
		objectMapper.writeValue(response.getWriter(), userResponse);
	}

	private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
		var session = request.getSession(false);
		
		if(session != null) {
			session.invalidate();
		}
		
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
	}

}
