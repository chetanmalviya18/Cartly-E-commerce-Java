package com.cartly.controller;

import java.io.IOException;
import java.util.List;

import com.cartly.dao.CategoryDAO;
import com.cartly.dto.CategoryResponse;
import com.cartly.service.CategoryService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@WebServlet("/api/categories/*")
public class CategoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CategoryService categoryService =
            new CategoryService(new CategoryDAO());

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        if (path == null || "/".equals(path)) {

            List<CategoryResponse> categories =
                    categoryService.getAllCategories();

            writeJson(response, categories);
            return;
        }

        try {

            Long id = Long.parseLong(path.substring(1));

            CategoryResponse category =
                    categoryService.getCategoryById(id);

            writeJson(response, category);

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid category ID");

        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
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