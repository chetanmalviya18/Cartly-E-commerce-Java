package com.cartly.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.cartly.entity.Product;
import com.cartly.util.HibernateUtil;

public class ProductDAO {
	public Product save(Product product) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()){
			
			Transaction transaction = session.beginTransaction();
			
			try {
                session.persist(product);
                transaction.commit();

            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
		}
		// Re-fetch with JOIN FETCH so category proxy is fully initialized
		return findById(product.getId());
	}
	
	public Product findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
        	return session.createQuery(
                    "SELECT p FROM Product p " +
                    "JOIN FETCH p.category " +
                    "WHERE p.id = :id",
                    Product.class)
                    .setParameter("id", id)
                    .uniqueResult();
        }
    }
	
	public List<Product> findAllActive() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {

        	return session.createQuery(
                    "SELECT p FROM Product p " +
                    "JOIN FETCH p.category " +
                    "WHERE p.active = true " +
                    "ORDER BY p.name",
                    Product.class)
                    .getResultList();
        }
	}
	
	
	public List<Product> findByCategory(Long categoryId){
		try (Session session = HibernateUtil.getSessionFactory().openSession()){
			return session.createQuery(
					"SELECT p FROM Product p " +
			                "JOIN FETCH p.category " +
			                "WHERE p.category.id = :categoryId " +
			                "AND p.active = true " +
			                "ORDER BY p.name",
			                Product.class)
			                .setParameter("categoryId", categoryId)
			                .getResultList();
		}
	}
	
	public List<Product> searchByName(String keyword){
		try (Session session = HibernateUtil.getSessionFactory().openSession()){
			
			return session.createQuery(
	                "SELECT p FROM Product p " +
	                "JOIN FETCH p.category " +
	                "WHERE LOWER(p.name) LIKE LOWER(:keyword) " +
	                "AND p.active = true " +
	                "ORDER BY p.name",
	                Product.class)
	                .setParameter("keyword", "%" + keyword + "%")
	                .getResultList();
		}
	}
	
	public Product update(Product product) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()){
			Transaction transaction = session.beginTransaction();
			
			try {
                session.merge(product);
                transaction.commit();

            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
		}
		// Re-fetch with JOIN FETCH so category proxy is fully initialized
		return findById(product.getId());
	}
	
	public Product findById(Session session, Long id) {
	    return session.createQuery(
	            "SELECT p FROM Product p " +
	            "JOIN FETCH p.category " +
	            "WHERE p.id = :id",
	            Product.class)
	            .setParameter("id", id)
	            .uniqueResult();
	}
	
	public void update(Session session, Product product) {
	    session.merge(product);
	}
}
