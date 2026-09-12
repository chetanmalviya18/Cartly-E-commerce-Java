package com.cartly.util;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class HibernateTransactionManager
        implements TransactionManager {

    private final Session session;
    private final Transaction transaction;

    public HibernateTransactionManager() {
        session = HibernateUtil.getSessionFactory().openSession();
        transaction = session.beginTransaction();
    }

    @Override
    public Session getSession() {
        return session;
    }

    @Override
    public void commit() {
        try {
            transaction.commit();
        } finally {
            session.close();
        }
    }

    @Override
    public void rollback() {
        try {
            if (transaction.isActive()) {
                transaction.rollback();
            }
        } finally {
            session.close();
        }
    }
}