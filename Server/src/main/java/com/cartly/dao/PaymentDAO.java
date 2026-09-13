package com.cartly.dao;

import com.cartly.entity.Payment;
import org.hibernate.Session;

public class PaymentDAO {

    public void save(Session session, Payment payment) {
        session.persist(payment);
    }

    public Payment findById(Session session, Long id) {
        return session.find(Payment.class, id);
    }

    public Payment findByOrderId(Session session, Long orderId) {
        return session.createQuery(
                "SELECT p FROM Payment p " +
                "WHERE p.order.id = :orderId",
                Payment.class
        )
        .setParameter("orderId", orderId)
        .uniqueResult();
    }

    public void update(Session session, Payment payment) {
        session.merge(payment);
    }
}