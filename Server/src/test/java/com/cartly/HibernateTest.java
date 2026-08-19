package com.cartly;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;

import com.cartly.util.HibernateUtil;

public class HibernateTest {
	
	@Test
	void testHibernateConnection() {
		SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
		assertNotNull(sessionFactory);
		
		try(Session session = sessionFactory.openSession()){
			assertNotNull(session);
			System.out.println("Hibernate session created successfully.");
		}
	}
}
