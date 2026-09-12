package com.cartly.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.cartly.entity.CartItem;
import com.cartly.util.HibernateUtil;

public class CartItemDAO {
	
	public void save(CartItem cartItem) {
		try(Session session = HibernateUtil.getSessionFactory().openSession()){
			Transaction transaction = session.beginTransaction();
			
			try {
                session.persist(cartItem);
                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
		}
	}
	
	public CartItem findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery(
                    "SELECT ci FROM CartItem ci " +
                    "JOIN FETCH ci.product " +
                    "WHERE ci.id = :id",
                    CartItem.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }
	
	public CartItem findByCartAndProduct(Long cartId, Long productId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery(
                    "SELECT ci FROM CartItem ci " +
                    "JOIN FETCH ci.product " +
                    "WHERE ci.cart.id = :cartId " +
                    "AND ci.product.id = :productId",
                    CartItem.class)
                    .setParameter("cartId", cartId)
                    .setParameter("productId", productId)
                    .uniqueResult();
        }
    }
	
	public List<CartItem> findByCartId(Long cartId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            return session.createQuery(
                    "SELECT ci FROM CartItem ci " +
                    "JOIN FETCH ci.product " +
                    "WHERE ci.cart.id = :cartId " +
                    "ORDER BY ci.id",
                    CartItem.class)
                    .setParameter("cartId", cartId)
                    .getResultList();
        }
    }
	
	public void update(CartItem cartItem) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Transaction transaction = session.beginTransaction();

            try {
                session.merge(cartItem);
                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }
	
	public void delete(CartItem cartItem) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

            Transaction transaction = session.beginTransaction();

            try {
                session.remove(session.merge(cartItem));
                transaction.commit();
            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }
	
	public CartItem findByCartAndProduct(Session session, Long cartId, Long productId) {
		return session.createQuery(
	            "SELECT ci FROM CartItem ci " +
	            "WHERE ci.cart.id = :cartId " +
	            "AND ci.product.id = :productId",
	            CartItem.class)
	            .setParameter("cartId", cartId)
	            .setParameter("productId", productId)
	            .uniqueResult();

	}
	
	public List<CartItem> findByCartId(
	        Session session,
	        Long cartId) {

	    return session.createQuery(
	            "SELECT ci FROM CartItem ci " +
	            "JOIN FETCH ci.product " +
	            "WHERE ci.cart.id = :cartId " +
	            "ORDER BY ci.id",
	            CartItem.class)
	            .setParameter("cartId", cartId)
	            .getResultList();
	}
	
	public void save(Session session, CartItem cartItem) {
	    session.persist(cartItem);
	}

	public void delete(Session session, CartItem cartItem) {
	    session.remove(cartItem);
	}
}
