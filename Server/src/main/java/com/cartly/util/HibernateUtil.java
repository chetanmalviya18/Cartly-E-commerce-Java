package com.cartly.util;

import java.io.InputStream;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;


public class HibernateUtil {
	
	private HibernateUtil() {
	}
	
	private static final SessionFactory sessionFactory = buildSessionFactory();
	
	private static SessionFactory buildSessionFactory() {
		try {
			Properties dbProperties = new Properties();
			
			try(InputStream input = 
					HibernateUtil.class
							.getClassLoader()
							.getResourceAsStream("db.properties")){
				if(input == null) {
					throw new RuntimeException("db.properties file not found");
				}
				
				dbProperties.load(input);
				
			}
			
			Configuration configuration = 
					new Configuration()
							.configure("hibernate.cfg.xml");
			
			configuration.setProperty("hibernate.connection.url", dbProperties.getProperty("db.url"));
			configuration.setProperty("hibernate.connection.username", dbProperties.getProperty("db.username"));
			configuration.setProperty("hibernate.connection.password",dbProperties.getProperty("db.password"));
			
			return configuration.buildSessionFactory();
			
		} catch (Throwable ex) {
			System.err.println("SessionFactory creation failed: " + ex);
            throw new ExceptionInInitializerError(ex);
		}
	}
	
	public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
	
	public static void shutdown() {
		 if (sessionFactory != null && !sessionFactory.isClosed()) {
	            getSessionFactory().close();
	     }
    }
}
