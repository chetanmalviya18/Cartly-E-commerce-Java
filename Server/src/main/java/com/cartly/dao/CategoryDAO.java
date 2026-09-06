package com.cartly.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.cartly.entity.Category;
import com.cartly.util.HibernateUtil;

public class CategoryDAO {
	public void save(Category category) {
		try(Session session = HibernateUtil.getSessionFactory().openSession()){
			Transaction transaction = session.beginTransaction();
			
			try {
				session.persist(category);
				transaction.commit();
			}catch (Exception e) {
				if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
			}
		}
	}
	
	public Category findById(Long id) {
		try(Session session = HibernateUtil.getSessionFactory().openSession()){
			return session.find(Category.class, id);
		}
	}
	
	public Category findByName(String name) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()){
			return session.createQuery(
					"FROM Category c WHERE c.name = :name",
					Category.class).setParameter("name", name).uniqueResult();
		}
	}
	
	public List<Category> findAll(){
		try (Session session = HibernateUtil.getSessionFactory().openSession()){
			return session.createQuery(
                    "FROM Category c ORDER BY c.name",
                    Category.class)
                    .getResultList();
		}
	}
	
	public void update(Category category) {
		try (Session session = HibernateUtil.getSessionFactory().openSession()) {
			Transaction transaction = session.beginTransaction();

			try {
                session.merge(category);
                transaction.commit();

            } catch (Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw e;
            }
		}
	}
}
