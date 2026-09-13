package com.cartly.controller;

import com.cartly.dto.PaymentRequest;
import com.cartly.dto.PaymentResponse;
import com.cartly.entity.Payment;
import com.cartly.service.PaymentService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

import tools.jackson.databind.ObjectMapper;

@WebServlet("/api/payments/*")
public class PaymentServlet extends HttpServlet {

    private PaymentService paymentService;
    private ObjectMapper objectMapper;

    @Override
    public void init() throws ServletException {
        paymentService = new PaymentService(
                new com.cartly.dao.PaymentDAO(),
                new com.cartly.dao.OrderDAO(),
                new com.cartly.dao.UserDAO(),
                new com.cartly.util.TransactionManagerFactory()
        );

        objectMapper = new ObjectMapper();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Long userId = getUserId(request);

        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required");
            return;
        }

        String path = request.getPathInfo();

        try {

            // POST /api/payments
            if (path == null || path.equals("/")) {

                PaymentRequest paymentRequest =
                        objectMapper.readValue(
                                request.getReader(),
                                PaymentRequest.class
                        );

                Payment payment = paymentService.createPayment(
                        userId,
                        paymentRequest.getOrderId(),
                        paymentRequest.getMethod()
                );

                sendJson(
                        response,
                        HttpServletResponse.SC_CREATED,
                        new PaymentResponse(payment)
                );

                return;
            }

            // POST /api/payments/{id}/process
            if (path.matches("/\\d+/process")) {

                String[] parts = path.split("/");

                Long paymentId = Long.parseLong(parts[1]);

                ProcessPaymentRequest processRequest =
                        objectMapper.readValue(
                                request.getReader(),
                                ProcessPaymentRequest.class
                        );

                Payment payment = paymentService.processPayment(
                        userId,
                        paymentId,
                        processRequest.isSuccess()
                );

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        new PaymentResponse(payment)
                );

                return;
            }

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Payment endpoint not found"
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error"
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Long userId = getUserId(request);

        if (userId == null) {
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Authentication required");
            return;
        }

        String path = request.getPathInfo();

        try {

            // GET /api/payments/{id}
            if (path != null && path.matches("/\\d+")) {

                Long paymentId =
                        Long.parseLong(path.substring(1));

                Payment payment =
                        paymentService.getPayment(userId, paymentId);

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        new PaymentResponse(payment)
                );

                return;
            }

            // GET /api/payments/order/{orderId}
            if (path != null && path.matches("/order/\\d+")) {

                Long orderId =
                        Long.parseLong(
                                path.substring("/order/".length())
                        );

                Payment payment =
                        paymentService.getPaymentByOrder(
                                userId,
                                orderId
                        );

                sendJson(
                        response,
                        HttpServletResponse.SC_OK,
                        new PaymentResponse(payment)
                );

                return;
            }

            sendError(
                    response,
                    HttpServletResponse.SC_NOT_FOUND,
                    "Payment endpoint not found"
            );

        } catch (IllegalArgumentException e) {

            sendError(
                    response,
                    HttpServletResponse.SC_BAD_REQUEST,
                    e.getMessage()
            );

        } catch (Exception e) {

            sendError(
                    response,
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Internal server error"
            );
        }
    }

    private Long getUserId(HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object userId = session.getAttribute("userId");

        if (userId instanceof Long) {
            return (Long) userId;
        }

        return null;
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

    public static class ProcessPaymentRequest {

        private boolean success;

        public ProcessPaymentRequest() {
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }
    }

    public static class ErrorResponse {

        private String message;

        public ErrorResponse() {
        }

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}