package com.cartly.util;

import org.hibernate.Session;

public interface TransactionManager {

    Session getSession();

    void commit();

    void rollback();
}