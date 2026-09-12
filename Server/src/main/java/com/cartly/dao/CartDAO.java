package com.cartly.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.cartly.entity.Cart;
import com.cartly.util.HibernateUtil;

public class CartDAO {
	public void save(Cart cart) {
		try(Session session = HibernateUtil.getSessionFactory().openSession()){
			
			Transaction transaction = session.beginTransaction();
			
			try {
				session.persist(cart);
				transaction.commit();
			}catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
		}
	}
	
	public Cart findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.find(Cart.class, id);
        }
    }
	
	public Cart findByUserId(Long userId) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()){
			
			return session.createQuery(
                    "SELECT c FROM Cart c " +
                    "JOIN FETCH c.user " +
                    "WHERE c.user.id = :userId",
                    Cart.class)
                    .setParameter("userId", userId)
                    .uniqueResult();
		}
	}
	
	public Cart findByUserId(Session session, Long userId) {
		return session.createQuery(
				"SELECT c FROM Cart c " +
				"WHERE c.user.id = :userId",
				Cart.class).setParameter("userId", userId).uniqueResult();
	}
	
	public void save(Session session, Cart cart) {
	    session.persist(cart);
	}
}
