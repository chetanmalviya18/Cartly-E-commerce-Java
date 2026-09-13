package com.cartly.controller;

import java.io.IOException;
import java.util.List;

import com.cartly.dao.AddressDAO;
import com.cartly.dao.CartDAO;
import com.cartly.dao.CartItemDAO;
import com.cartly.dao.OrderDAO;
import com.cartly.dao.OrderItemDAO;
import com.cartly.dao.ProductDAO;
import com.cartly.dao.UserDAO;
import com.cartly.dto.OrderResponse;
import com.cartly.entity.OrderStatus;
import com.cartly.service.OrderService;
import com.cartly.util.TransactionManagerFactory;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

@WebServlet("/api/admin/orders/*")
public class AdminOrderServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final OrderService orderService =
            new OrderService(
                    new OrderDAO(),
                    new OrderItemDAO(),
                    new CartDAO(),
                    new CartItemDAO(),
                    new ProductDAO(),
                    new AddressDAO(),
                    new UserDAO(),
                    new TransactionManagerFactory()
            );

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        try {

            // GET /api/admin/orders
            if (path == null || "/".equals(path)) {

                List<OrderResponse> orders =
                        orderService.getAllOrdersForAdmin();

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        orders
                );

                return;
            }

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Admin order endpoint not found"
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @Override
    protected void doPut(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getPathInfo();

        if (path == null || "/".equals(path)) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Order ID is required"
            );

            return;
        }

        try {

            Long orderId =
                    Long.parseLong(path.substring(1));

            UpdateOrderStatusRequest statusRequest =
                    objectMapper.readValue(
                            request.getReader(),
                            UpdateOrderStatusRequest.class
                    );

            if (statusRequest == null ||
                    statusRequest.getStatus() == null) {

                throw new IllegalArgumentException(
                        "Order status is required"
                );
            }

            OrderResponse order =
                    orderService.updateOrderStatus(
                            orderId,
                            statusRequest.getStatus()
                    );

            sendJson(
                    response,
                    HttpServletResponse.SC_OK,
                    order
            );

        } catch (NumberFormatException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid order ID"
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    private void sendJson(
            HttpServletResponse response,
            int status,
            Object data)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                data
        );
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {

        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(
                response.getWriter(),
                new ErrorResponse(message)
        );
    }

    public static class UpdateOrderStatusRequest {

        private OrderStatus status;

        public UpdateOrderStatusRequest() {
        }

        public OrderStatus getStatus() {
            return status;
        }

        public void setStatus(OrderStatus status) {
            this.status = status;
        }
    }

    public static class ErrorResponse {

        private final String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}