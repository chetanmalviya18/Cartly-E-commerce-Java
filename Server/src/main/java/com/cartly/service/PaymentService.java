package com.cartly.service;

import com.cartly.dao.OrderDAO;
import com.cartly.dao.PaymentDAO;
import com.cartly.dao.UserDAO;
import com.cartly.entity.Order;
import com.cartly.entity.Payment;
import com.cartly.entity.PaymentMethod;
import com.cartly.entity.PaymentStatus;
import com.cartly.entity.User;
import com.cartly.util.HibernateTransactionManager;
import com.cartly.util.TransactionManager;
import com.cartly.util.TransactionManagerFactory;

import java.math.BigDecimal;
import java.util.UUID;

import org.hibernate.Session;

public class PaymentService {

    private final PaymentDAO paymentDAO;
    private final OrderDAO orderDAO;
    private final UserDAO userDAO;
    private final TransactionManagerFactory transactionManagerFactory;

    public PaymentService(
            PaymentDAO paymentDAO,
            OrderDAO orderDAO,
            UserDAO userDAO,
            TransactionManagerFactory transactionManagerFactory) {

        this.paymentDAO = paymentDAO;
        this.orderDAO = orderDAO;
        this.userDAO = userDAO;
        this.transactionManagerFactory = transactionManagerFactory;
    }

    public Payment createPayment(Long userId, Long orderId, PaymentMethod method) {

        validateIds(userId, orderId);

        if (method == null) {
            throw new IllegalArgumentException("Payment method is required");
        }

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            User user = userDAO.findById(session, userId);

            if (user == null || !user.isActive()) {
                throw new IllegalArgumentException("User not found or inactive");
            }

            Order order = orderDAO.findById(session, orderId);

            if (order == null) {
                throw new IllegalArgumentException("Order not found");
            }

            if (!order.getUser().getId().equals(userId)) {
                throw new IllegalArgumentException(
                        "You are not authorized to pay for this order");
            }

            Payment existingPayment =
                    paymentDAO.findByOrderId(session, orderId);

            if (existingPayment != null) {
                throw new IllegalArgumentException(
                        "Payment already exists for this order");
            }

            BigDecimal amount = order.getTotalAmount();

            if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException(
                        "Order amount must be greater than zero");
            }

            Payment payment = new Payment();

            payment.setOrder(order);
            payment.setMethod(method);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setAmount(amount);

            paymentDAO.save(session, payment);

            transactionManager.commit();

            return payment;

        } catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }

    public Payment processPayment(
            Long userId,
            Long paymentId,
            boolean success) {

        validateIds(userId, paymentId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            User user = userDAO.findById(session, userId);

            if (user == null || !user.isActive()) {
                throw new IllegalArgumentException("User not found or inactive");
            }

            Payment payment = paymentDAO.findById(session, paymentId);

            if (payment == null) {
                throw new IllegalArgumentException("Payment not found");
            }

            Long paymentUserId =
                    payment.getOrder().getUser().getId();

            if (!paymentUserId.equals(userId)) {
                throw new IllegalArgumentException(
                        "You are not authorized to process this payment");
            }

            if (payment.getStatus() != PaymentStatus.PENDING) {
                throw new IllegalArgumentException(
                        "Payment has already been processed");
            }

            if (success) {

                payment.setStatus(PaymentStatus.SUCCESS);

                payment.setTransactionReference(
                        generateTransactionReference());

            } else {

                payment.setStatus(PaymentStatus.FAILED);
            }

            paymentDAO.update(session, payment);

            transactionManager.commit();

            return payment;

        } catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }

    public Payment getPayment(Long userId, Long paymentId) {

        validateIds(userId, paymentId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Payment payment = paymentDAO.findById(session, paymentId);

            if (payment == null) {
                throw new IllegalArgumentException("Payment not found");
            }

            Long paymentUserId =
                    payment.getOrder().getUser().getId();

            if (!paymentUserId.equals(userId)) {
                throw new IllegalArgumentException(
                        "You are not authorized to view this payment");
            }

            transactionManager.commit();

            return payment;

        } catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }

    public Payment getPaymentByOrder(Long userId, Long orderId) {

        validateIds(userId, orderId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Payment payment =
                    paymentDAO.findByOrderId(session, orderId);

            if (payment == null) {
                throw new IllegalArgumentException("Payment not found");
            }

            Long paymentUserId =
                    payment.getOrder().getUser().getId();

            if (!paymentUserId.equals(userId)) {
                throw new IllegalArgumentException(
                        "You are not authorized to view this payment");
            }

            transactionManager.commit();

            return payment;

        } catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }

    private void validateIds(Long userId, Long id) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid ID");
        }
    }

    private String generateTransactionReference() {

        return "TXN-" + UUID.randomUUID();
    }
}