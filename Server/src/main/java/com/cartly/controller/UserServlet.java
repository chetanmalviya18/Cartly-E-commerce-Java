package com.cartly.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.cartly.dao.UserDAO;
import com.cartly.dto.UserResponse;
import com.cartly.service.UserService;
import tools.jackson.databind.ObjectMapper;

@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private final UserService userService;
    private final ObjectMapper objectMapper;

    public UserServlet() {
        this(new UserService(new UserDAO()), new ObjectMapper());
    }

    public UserServlet(UserService userService, ObjectMapper objectMapper) {
        this.userService = userService;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String path = request.getPathInfo();

        if ("/me".equals(path)) {
            getCurrentUser(request, response);
        } else {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Endpoint not found"
            );
        }
    }

    private void getCurrentUser(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var session = request.getSession(false);
        Long userId = (Long) session.getAttribute("userId");

        UserResponse userResponse = userService.getUserById(userId);

        if (userResponse == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "User not found"
            );
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), userResponse);
    }
}
