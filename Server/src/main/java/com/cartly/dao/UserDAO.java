package com.cartly.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;

import com.cartly.entity.User;
import com.cartly.util.HibernateUtil;

public class UserDAO {
	public void save(User user) {
		Transaction transaction = null;
		
		try(Session session =
				HibernateUtil.getSessionFactory().openSession()){
			
			transaction = session.beginTransaction();
			
			try {
				session.persist(user);
				transaction.commit();
			} catch (Exception e) {
				if(transaction.isActive()) {
					transaction.rollback();
				}
				throw e;
			}
		} 
	}
	
	public User findByEmail(String email) {
		try(Session session = 
				HibernateUtil.getSessionFactory().openSession()){
			
			return session.createQuery("FROM User u WHERE u.email = :email", User.class)
												.setParameter("email", email).uniqueResult();
		}
	}
	
	public User findById(Long id) {
		try(Session session =
				HibernateUtil.getSessionFactory().openSession()){
			
			return session.find(User.class, id);
		}
	}
}
