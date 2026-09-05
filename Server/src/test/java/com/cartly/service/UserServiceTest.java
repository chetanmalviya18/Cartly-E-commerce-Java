package com.cartly.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.cartly.dao.UserDAO;
import com.cartly.dto.UserResponse;
import com.cartly.entity.User;
import com.cartly.entity.UserRole;

public class UserServiceTest {

    @Test
    void getUserById_shouldReturnUserResponse_whenUserExists() {
        UserDAO userDAO = mock(UserDAO.class);
        UserService userService = new UserService(userDAO);

        User user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setRole(UserRole.CUSTOMER);

        when(userDAO.findById(1L)).thenReturn(user);

        UserResponse response = userService.getUserById(1L);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(UserRole.CUSTOMER, response.getRole());

        verify(userDAO).findById(1L);
    }

    @Test
    void getUserById_shouldReturnNull_whenUserDoesNotExist() {
        UserDAO userDAO = mock(UserDAO.class);
        UserService userService = new UserService(userDAO);

        when(userDAO.findById(999L)).thenReturn(null);

        UserResponse response = userService.getUserById(999L);

        assertNull(response);

        verify(userDAO).findById(999L);
    }

    @Test
    void getUserById_shouldReturnNull_whenUserIdIsNull() {
        UserDAO userDAO = mock(UserDAO.class);
        UserService userService = new UserService(userDAO);

        UserResponse response = userService.getUserById(null);

        assertNull(response);
    }
}
