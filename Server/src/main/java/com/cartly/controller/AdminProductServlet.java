package com.cartly.controller;

import java.io.IOException;

import com.cartly.dao.CategoryDAO;
import com.cartly.dao.ProductDAO;
import com.cartly.dto.ProductRequest;
import com.cartly.dto.ProductResponse;
import com.cartly.service.ProductService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@WebServlet("/api/admin/products/*")
public class AdminProductServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ProductService productService =
            new ProductService(
                    new ProductDAO(),
                    new CategoryDAO());

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        try {

            ProductRequest productRequest =
                    objectMapper.readValue(
                            request.getReader(),
                            ProductRequest.class);

            ProductResponse product =
                    productService.createProduct(
                            productRequest.getName(),
                            productRequest.getDescription(),
                            productRequest.getPrice(),
                            productRequest.getStock(),
                            productRequest.getImageUrl(),
                            productRequest.getCategoryId());

            response.setStatus(
                    HttpServletResponse.SC_CREATED);

            writeJson(response, product);

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
                    "Product ID is required");

            return;
        }

        try {

            Long id =
                    Long.parseLong(path.substring(1));

            ProductRequest productRequest =
                    objectMapper.readValue(
                            request.getReader(),
                            ProductRequest.class);

            ProductResponse product =
                    productService.updateProduct(
                            id,
                            productRequest.getName(),
                            productRequest.getDescription(),
                            productRequest.getPrice(),
                            productRequest.getStock(),
                            productRequest.getImageUrl(),
                            productRequest.getCategoryId());

            writeJson(response, product);

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid product ID");

        } catch (IllegalArgumentException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage());
        }
    }

    @Override
    protected void doDelete(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        if (path == null || "/".equals(path)) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Product ID is required");

            return;
        }

        try {

            Long id =
                    Long.parseLong(path.substring(1));

            productService.deactivateProduct(id);

            response.setStatus(
                    HttpServletResponse.SC_NO_CONTENT);

        } catch (NumberFormatException e) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid product ID");

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