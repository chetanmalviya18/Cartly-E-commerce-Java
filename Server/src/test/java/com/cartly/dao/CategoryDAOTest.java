package com.cartly.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cartly.entity.Category;
import com.cartly.util.HibernateUtil;

public class CategoryDAOTest {

    private final CategoryDAO categoryDAO = new CategoryDAO();

    @BeforeEach
    @AfterEach
    void cleanDatabase() {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.createNativeQuery("DELETE FROM products", void.class).executeUpdate();
            session.createNativeQuery("DELETE FROM categories", void.class).executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
        }
    }

    @Test
    void saveCategory_shouldPersistNameAndDescription() {
        Category category = new Category();
        category.setName("Electronics Test");
        category.setDescription("Electronic devices and accessories");

        categoryDAO.save(category);

        assertNotNull(category.getId());

        Category fetchedCategory = categoryDAO.findById(category.getId());
        assertNotNull(fetchedCategory);
        assertEquals("Electronics Test", fetchedCategory.getName());
        assertEquals("Electronic devices and accessories", fetchedCategory.getDescription());
    }
}
