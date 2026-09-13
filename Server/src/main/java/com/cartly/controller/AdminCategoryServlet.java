package com.cartly.controller;

import java.io.IOException;

import com.cartly.dao.CategoryDAO;
import com.cartly.dto.CategoryRequest;
import com.cartly.dto.CategoryResponse;
import com.cartly.service.CategoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@WebServlet("/api/admin/categories/*")
public class AdminCategoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CategoryService categoryService =
            new CategoryService(new CategoryDAO());

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            CategoryRequest categoryRequest =
                    objectMapper.readValue(
                            request.getReader(),
                            CategoryRequest.class);

            CategoryResponse category =
                    categoryService.createCategory(
                            categoryRequest.getName(),
                            categoryRequest.getDescription());

            response.setStatus(
                    HttpServletResponse.SC_CREATED);

            writeJson(response, category);

        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        if (path == null || "/".equals(path)) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Category ID is required");

            return;
        }

        try {

            Long id =
                    Long.parseLong(path.substring(1));

            CategoryRequest categoryRequest =
                    objectMapper.readValue(
                            request.getReader(),
                            CategoryRequest.class);

            CategoryResponse category =
                    categoryService.updateCategory(
                            id,
                            categoryRequest.getName(),
                            categoryRequest.getDescription());

            writeJson(response, category);

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid category ID");

        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());
        }
    }

    private void writeJson(
            HttpServletResponse response,
            Object data)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                data);
    }
}