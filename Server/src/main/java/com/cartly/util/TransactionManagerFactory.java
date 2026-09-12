package com.cartly.util;

public class TransactionManagerFactory {

    public TransactionManager create() {
        return new HibernateTransactionManager();
    }
}