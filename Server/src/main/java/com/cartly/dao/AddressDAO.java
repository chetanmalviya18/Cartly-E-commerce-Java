package com.cartly.dao;

import com.cartly.entity.Address;
import org.hibernate.Session;

import java.util.List;

public class AddressDAO {

    public void save(Session session, Address address) {
        session.persist(address);
    }

    public Address findById(Session session, Long id) {
        return session.find(Address.class, id);
    }

    public List<Address> findByUserId(Session session, Long userId) {
        return session.createQuery(
                "SELECT a FROM Address a " +
                "WHERE a.user.id = :userId " +
                "ORDER BY a.id",
                Address.class
        )
        .setParameter("userId", userId)
        .getResultList();
    }

    public void update(Session session, Address address) {
        session.merge(address);
    }

    public void delete(Session session, Address address) {
        session.remove(address);
    }
}