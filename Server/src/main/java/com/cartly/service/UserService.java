package com.cartly.service;

import com.cartly.dao.UserDAO;
import com.cartly.dto.UserResponse;
import com.cartly.entity.User;

public class UserService {

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public UserResponse getUserById(Long userId) {
        if (userId == null) {
            return null;
        }

        User user = userDAO.findById(userId);

        if (user == null) {
            return null;
        }

        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setName(user.getName());
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole());

        return userResponse;
    }
}
