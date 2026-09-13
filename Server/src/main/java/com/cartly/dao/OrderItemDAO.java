package com.cartly.dao;

import com.cartly.entity.OrderItem;
import org.hibernate.Session;

import java.util.List;

public class OrderItemDAO {

    public void save(Session session, OrderItem orderItem) {
        session.persist(orderItem);
    }

    public List<OrderItem> findByOrderId(
            Session session,
            Long orderId) {

        return session.createQuery(
                "SELECT oi FROM OrderItem oi " +
                "JOIN FETCH oi.product " +
                "WHERE oi.order.id = :orderId " +
                "ORDER BY oi.id",
                OrderItem.class
        )
        .setParameter("orderId", orderId)
        .getResultList();
    }
}