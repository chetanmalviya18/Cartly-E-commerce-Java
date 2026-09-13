package com.cartly.controller;

import com.cartly.dao.AddressDAO;
import com.cartly.dao.CartDAO;
import com.cartly.dao.CartItemDAO;
import com.cartly.dao.OrderDAO;
import com.cartly.dao.OrderItemDAO;
import com.cartly.dao.ProductDAO;
import com.cartly.dao.UserDAO;
import com.cartly.dto.OrderResponse;
import com.cartly.dto.PlaceOrderRequest;
import com.cartly.service.OrderService;
import com.cartly.util.TransactionManagerFactory;
import tools.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebServlet("/api/orders/*")
public class OrderServlet extends HttpServlet {

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
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Long userId = getUserId(request, response);

        if (userId == null) {
            return;
        }

        try {
            PlaceOrderRequest orderRequest =
                    objectMapper.readValue(
                            request.getReader(),
                            PlaceOrderRequest.class
                    );

            if (orderRequest == null ||
                    orderRequest.getAddressId() == null) {

                throw new IllegalArgumentException(
                        "Address ID is required"
                );
            }

            OrderResponse order =
                    orderService.placeOrderResponse(
                            userId,
                            orderRequest.getAddressId()
                    );

            sendJson(
                    response,
                    HttpServletResponse.SC_CREATED,
                    order
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
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Long userId = getUserId(request, response);

        if (userId == null) {
            return;
        }

        String path = request.getPathInfo();

        try {

            if (path == null || path.equals("/")) {

                List<OrderResponse> orders =
                        orderService.getUserOrderResponses(userId);

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        orders
                );

            } else {

                Long orderId = parseId(path);

                OrderResponse order =
                        orderService.getOrderResponse(
                                userId,
                                orderId
                        );

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        order
                );
            }

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    private Long getUserId(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("userId") == null) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required"
            );

            return null;
        }

        Object userId =
                session.getAttribute("userId");

        if (!(userId instanceof Long)) {

            sendError(
                    response,
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "Invalid authentication session"
            );

            return null;
        }

        return (Long) userId;
    }

    private Long parseId(String path) {

        if (path == null ||
                path.equals("/") ||
                path.length() <= 1) {

            throw new IllegalArgumentException(
                    "Order ID is required"
            );
        }

        try {
            long id = Long.parseLong(path.substring(1));

            if (id <= 0) {
                throw new IllegalArgumentException(
                        "Invalid order ID"
                );
            }

            return id;

        } catch (NumberFormatException e) {

            throw new IllegalArgumentException(
                    "Invalid order ID"
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