package com.cartly.service;

import java.util.List;

import org.hibernate.Session;

import com.cartly.dao.AddressDAO;
import com.cartly.dao.UserDAO;
import com.cartly.entity.Address;
import com.cartly.entity.User;
import com.cartly.util.TransactionManager;
import com.cartly.util.TransactionManagerFactory;

public class AddressService {
	
	private final AddressDAO addressDAO;
    private final UserDAO userDAO;
    private final TransactionManagerFactory transactionManagerFactory;
    
    public AddressService(
            AddressDAO addressDAO,
            UserDAO userDAO,
            TransactionManagerFactory transactionManagerFactory) {

        this.addressDAO = addressDAO;
        this.userDAO = userDAO;
        this.transactionManagerFactory = transactionManagerFactory;
    }
    
    public Address addAddress(Long userId, Address address) {
    	
    	validateUserId(userId);
        validateAddress(address);
        
        TransactionManager transactionManager =
                transactionManagerFactory.create();
        
        try {
        	Session session = transactionManager.getSession();

            User user = userDAO.findById(session, userId);
            
            if (user == null || !user.isActive()) {
                throw new IllegalArgumentException("User not found");
            }
            
            address.setUser(user);

            addressDAO.save(session, address);

            transactionManager.commit();

            return address;
        } catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }
    
    public List<Address> getUserAddresses(Long userId){
    	
    	validateUserId(userId);
    	
    	TransactionManager transactionManager =
                transactionManagerFactory.create();
    	
    	try {
            Session session = transactionManager.getSession();

            User user = userDAO.findById(session, userId);

            if (user == null || !user.isActive()) {
                throw new IllegalArgumentException("User not found");
            }

            List<Address> addresses =
                    addressDAO.findByUserId(session, userId);

            transactionManager.commit();

            return addresses;

        } catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }
    
    public Address getAddress(Long userId, Long addressId) {

        validateUserId(userId);
        validateAddressId(addressId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Address address = addressDAO.findById(session, addressId);

            if (address == null ||
                    !address.getUser().getId().equals(userId)) {

                throw new IllegalArgumentException("Address not found");
            }

            transactionManager.commit();

            return address;

        } catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }
    
    public void updateAddress(
            Long userId,
            Long addressId,
            Address updatedAddress) {

        validateUserId(userId);
        validateAddressId(addressId);
        validateAddress(updatedAddress);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Address existingAddress =
                    addressDAO.findById(session, addressId);

            if (existingAddress == null ||
                    !existingAddress.getUser().getId().equals(userId)) {

                throw new IllegalArgumentException("Address not found");
            }

            existingAddress.setFullName(updatedAddress.getFullName());
            existingAddress.setPhone(updatedAddress.getPhone());
            existingAddress.setAddressLine(updatedAddress.getAddressLine());
            existingAddress.setCity(updatedAddress.getCity());
            existingAddress.setState(updatedAddress.getState());
            existingAddress.setPincode(updatedAddress.getPincode());

            addressDAO.update(session, existingAddress);

            transactionManager.commit();

        } catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }
    
    public void deleteAddress(Long userId, Long addressId) {

        validateUserId(userId);
        validateAddressId(addressId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Address address =
                    addressDAO.findById(session, addressId);

            if (address == null ||
                    !address.getUser().getId().equals(userId)) {

                throw new IllegalArgumentException("Address not found");
            }

            addressDAO.delete(session, address);

            transactionManager.commit();

        } catch (RuntimeException e) {
            transactionManager.rollback();
            throw e;
        }
    }
    
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("Invalid user ID");
        }
    }

    private void validateAddressId(Long addressId) {
        if (addressId == null || addressId <= 0) {
            throw new IllegalArgumentException("Invalid address ID");
        }
    }

    private void validateAddress(Address address) {

        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }

        if (isBlank(address.getFullName())) {
            throw new IllegalArgumentException("Full name is required");
        }

        if (isBlank(address.getPhone())) {
            throw new IllegalArgumentException("Phone is required");
        }

        if (isBlank(address.getAddressLine())) {
            throw new IllegalArgumentException("Address line is required");
        }

        if (isBlank(address.getCity())) {
            throw new IllegalArgumentException("City is required");
        }

        if (isBlank(address.getState())) {
            throw new IllegalArgumentException("State is required");
        }

        if (isBlank(address.getPincode())) {
            throw new IllegalArgumentException("Pincode is required");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

}
