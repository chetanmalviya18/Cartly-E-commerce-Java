package com.cartly.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cartly.entity.User;
import com.cartly.entity.UserRole;
import com.cartly.util.HibernateUtil;

public class UserDAOTest {
	private final UserDAO userDAO = new UserDAO();
	
	@BeforeEach
	@AfterEach
	void cleanDatabase() {
		Transaction transaction = null;
		try(Session session = 
				HibernateUtil.getSessionFactory().openSession()){
			
			transaction = session.beginTransaction();
			session.createNativeQuery("DELETE FROM users", void.class).executeUpdate();
			transaction.commit();
		} catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
        }
	}
	
	@Test
	void saveUser_shouldPersistUser() {
		User user = new User();
		
		user.setName("Test User");
		user.setEmail("test1@cartly.com");
		user.setPassword("dummy-password");
		user.setRole(UserRole.CUSTOMER);
		user.setActive(true);
		
		userDAO.save(user);
		
		assertNotNull(user.getId());
		
	}
	
	@Test
	void findUserByEmail_shouldFindUser() {

	    User user = new User();

	    user.setName("Find Test");
	    user.setEmail("findtest@cartly.com");
	    user.setPassword("dummy-password");
	    user.setRole(UserRole.CUSTOMER);
	    user.setActive(true);

	    userDAO.save(user);

	    User foundUser = userDAO.findByEmail("findtest@cartly.com");

	    assertNotNull(foundUser);
	    assertNotNull(foundUser.getId());
	}
	
	@Test
	void findUserById_shouldFindUser() {

	    User user = new User();

	    user.setName("Find By ID Test");
	    user.setEmail("findbyid@cartly.com");
	    user.setPassword("dummy-password");
	    user.setRole(UserRole.CUSTOMER);
	    user.setActive(true);

	    userDAO.save(user);

	    User foundUser = userDAO.findById(user.getId());

	    assertNotNull(foundUser);
	    assertEquals(user.getId(), foundUser.getId());
	}
}
