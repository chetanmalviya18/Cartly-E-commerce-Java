package com.cartly.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.cartly.dao.UserDAO;
import com.cartly.dto.LoginRequest;
import com.cartly.dto.RegisterRequest;
import com.cartly.dto.UserResponse;
import com.cartly.entity.User;
import com.cartly.entity.UserRole;
import com.cartly.util.PasswordUtil;

public class AuthServiceTest {
	@Test
	void register_shouldCreateUserSuccessfully() {
		UserDAO userDAO = mock(UserDAO.class);
		AuthService authService = new AuthService(userDAO);
		
		RegisterRequest request = new RegisterRequest();
		request.setName("Test User");
		request.setEmail("TEST@Cartly.com");
		request.setPassword("password123");
		
		when(userDAO.findByEmail("test@cartly.com")).thenReturn(null);
		
		doAnswer(invocation -> {
			User user = invocation.getArgument(0);
			user.setId(100L);
			return null;
		}).when(userDAO).save(any(User.class));
		
		UserResponse response = authService.register(request);
		
		assertNotNull(response);
		assertEquals(100L, response.getId());
		assertEquals("Test User", response.getName());
	    assertEquals("test@cartly.com", response.getEmail());
	    assertEquals("CUSTOMER", response.getRole().name());
	    
	    verify(userDAO).findByEmail("test@cartly.com");
        verify(userDAO).save(any(User.class));
	}
	
	@Test
	void register_shouldRejectDuplicateEmail() {
		UserDAO userDAO = mock(UserDAO.class);
		AuthService authService = new AuthService(userDAO);
		
		RegisterRequest request = new RegisterRequest();
		request.setName("Another User");
		request.setEmail("TEST@Cartly.com");
	    request.setPassword("password123");
	    
	    User existingUser = new User();
	    
	    when(userDAO.findByEmail("test@cartly.com")).thenReturn(existingUser);
	    
	    IllegalArgumentException exception = 
	    		assertThrows(
	    				IllegalArgumentException.class,
	    				() -> authService.register(request)
	    		);
	    
	    assertEquals("Email already registered", exception.getMessage());
	    
	    verify(userDAO).findByEmail("test@cartly.com");
	    verify(userDAO, never()).save(any(User.class));
	}
	
	@Test
	void register_shouldHashPassword() {
		UserDAO userDAO = mock(UserDAO.class);
	    AuthService authService = new AuthService(userDAO);
	    
	    RegisterRequest request = new RegisterRequest();
	    request.setName("Password Test");
	    request.setEmail("password@cartly.com");
	    request.setPassword("password123");
	    
	    when(userDAO.findByEmail("password@cartly.com"))
        .thenReturn(null);
	    
	    authService.register(request);
	    
	    ArgumentCaptor<User> captor =
	            ArgumentCaptor.forClass(User.class);
	    
	    verify(userDAO).save(captor.capture());

	    User savedUser = captor.getValue();

	    assertNotEquals("password123", savedUser.getPassword());
	    assertTrue(
	            PasswordUtil.verifyPassword(
	                    "password123",
	                    savedUser.getPassword()
	            )
	    );
	}
	
	@Test
	void login_shouldSucceedWithValidCredentials() {
		UserDAO userDAO = mock(UserDAO.class);
		AuthService authService = new AuthService(userDAO);
		
		String password = "password123";
		
		User user = new User();
		user.setId(1L);
	    user.setName("Test User");
	    user.setEmail("test@cartly.com");
	    user.setPassword(PasswordUtil.hashPassword(password));
	    user.setRole(UserRole.CUSTOMER);
	    user.setActive(true);
	    
	    when(userDAO.findByEmail("test@cartly.com")).thenReturn(user);
	    
	    LoginRequest request = new LoginRequest();
	    request.setEmail("TEST@Cartly.com");
	    request.setPassword(password);
	    
	    UserResponse response = authService.login(request);
	    
	    assertNotNull(response);
	    assertEquals(1L, response.getId());
	    assertEquals("Test User", response.getName());
	    assertEquals("test@cartly.com", response.getEmail());
	    assertEquals(UserRole.CUSTOMER, response.getRole());
	    
	    verify(userDAO).findByEmail("test@cartly.com");
	}
	
	@Test
	void login_shouldRejectWrongPassword() {
		UserDAO userDAO = mock(UserDAO.class);
		AuthService authService = new AuthService(userDAO);
		
		User user = new User();
	    user.setEmail("test@cartly.com");
	    user.setPassword(PasswordUtil.hashPassword("correctPassword"));
	    user.setRole(UserRole.CUSTOMER);
	    user.setActive(true);
	    
	    when(userDAO.findByEmail("test@cartly.com"))
        .thenReturn(user);
	    
	    LoginRequest request = new LoginRequest();
	    request.setEmail("test@cartly.com");
	    request.setPassword("wrongPassword");
	    
	    IllegalArgumentException exception =
	            assertThrows(
	                    IllegalArgumentException.class,
	                    () -> authService.login(request)
	            );
	    assertEquals(
	            "Invalid email or password",
	            exception.getMessage()
	    );
	}
	
	@Test
	void login_shouldRejectUnknownEmail() {
		UserDAO userDAO = mock(UserDAO.class);
	    AuthService authService = new AuthService(userDAO);
	    
	    when(userDAO.findByEmail("unknown@cartly.com"))
        .thenReturn(null);
	    
	    LoginRequest request = new LoginRequest();
	    request.setEmail("unknown@cartly.com");
	    request.setPassword("password123");
	    
	    IllegalArgumentException exception =
	            assertThrows(
	                    IllegalArgumentException.class,
	                    () -> authService.login(request)
	            );
	    
	    assertEquals(
	            "Invalid email or password",
	            exception.getMessage()
	    );
	    
	    verify(userDAO).findByEmail("unknown@cartly.com");
	}
	
	@Test
	void login_shouldRejectInactiveUser() {

	    UserDAO userDAO = mock(UserDAO.class);
	    AuthService authService = new AuthService(userDAO);

	    User user = new User();
	    user.setEmail("inactive@cartly.com");
	    user.setPassword(PasswordUtil.hashPassword("password123"));
	    user.setRole(UserRole.CUSTOMER);
	    user.setActive(false);

	    when(userDAO.findByEmail("inactive@cartly.com"))
	            .thenReturn(user);

	    LoginRequest request = new LoginRequest();
	    request.setEmail("inactive@cartly.com");
	    request.setPassword("password123");

	    IllegalArgumentException exception =
	            assertThrows(
	                    IllegalArgumentException.class,
	                    () -> authService.login(request)
	            );

	    assertEquals(
	            "User account is inactive",
	            exception.getMessage()
	    );
	}
}
