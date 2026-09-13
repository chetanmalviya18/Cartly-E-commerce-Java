package com.cartly.dao;

import com.cartly.entity.Order;
import org.hibernate.Session;

import java.util.List;

public class OrderDAO {

    public void save(Session session, Order order) {
        session.persist(order);
    }

    public Order findById(Session session, Long id) {
        return session.find(Order.class, id);
    }

    public List<Order> findByUserId(Session session, Long userId) {
        return session.createQuery(
                "SELECT o FROM Order o " +
                "WHERE o.user.id = :userId " +
                "ORDER BY o.id DESC",
                Order.class
        )
        .setParameter("userId", userId)
        .getResultList();
    }

    public void update(Session session, Order order) {
        session.merge(order);
    }
}