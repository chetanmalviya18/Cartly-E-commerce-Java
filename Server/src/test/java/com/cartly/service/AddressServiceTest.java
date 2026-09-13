package com.cartly.service;

import com.cartly.dao.AddressDAO;
import com.cartly.dao.UserDAO;
import com.cartly.entity.Address;
import com.cartly.entity.User;
import com.cartly.util.TransactionManager;
import com.cartly.util.TransactionManagerFactory;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddressServiceTest {

    private AddressDAO addressDAO;
    private UserDAO userDAO;
    private TransactionManagerFactory transactionManagerFactory;
    private TransactionManager transactionManager;
    private Session session;

    private AddressService addressService;

    @BeforeEach
    void setUp() {
        addressDAO = mock(AddressDAO.class);
        userDAO = mock(UserDAO.class);
        transactionManagerFactory = mock(TransactionManagerFactory.class);
        transactionManager = mock(TransactionManager.class);
        session = mock(Session.class);

        when(transactionManagerFactory.create())
                .thenReturn(transactionManager);

        when(transactionManager.getSession())
                .thenReturn(session);

        addressService = new AddressService(
                addressDAO,
                userDAO,
                transactionManagerFactory
        );
    }

    // ---------------------------------------------------------
    // ADD ADDRESS
    // ---------------------------------------------------------

    @Test
    void addAddress_shouldSaveAddress() {

        Long userId = 1L;
        User user = createUser(userId, true);
        Address address = createAddress();

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        addressService.addAddress(userId, address);

        assertSame(user, address.getUser());

        verify(addressDAO).save(session, address);
        verify(transactionManager).commit();
        verify(transactionManager, never()).rollback();
    }

    @Test
    void addAddress_shouldFailWhenUserDoesNotExist() {

        Long userId = 1L;
        Address address = createAddress();

        when(userDAO.findById(session, userId))
                .thenReturn(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.addAddress(userId, address)
                );

        assertEquals("User not found", exception.getMessage());

        verify(addressDAO, never()).save(any(), any());
        verify(transactionManager).rollback();
        verify(transactionManager, never()).commit();
    }

    @Test
    void addAddress_shouldFailWhenUserIsInactive() {

        Long userId = 1L;
        User user = createUser(userId, false);
        Address address = createAddress();

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.addAddress(userId, address)
                );

        assertEquals("User not found", exception.getMessage());

        verify(addressDAO, never()).save(any(), any());
        verify(transactionManager).rollback();
        verify(transactionManager, never()).commit();
    }

    // ---------------------------------------------------------
    // GET USER ADDRESSES
    // ---------------------------------------------------------

    @Test
    void getUserAddresses_shouldReturnAddresses() {

        Long userId = 1L;
        User user = createUser(userId, true);

        Address address1 = createAddress();
        Address address2 = createAddress();

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(addressDAO.findByUserId(session, userId))
                .thenReturn(List.of(address1, address2));

        List<Address> result =
                addressService.getUserAddresses(userId);

        assertEquals(2, result.size());
        assertSame(address1, result.get(0));
        assertSame(address2, result.get(1));

        verify(addressDAO).findByUserId(session, userId);
        verify(transactionManager).commit();
        verify(transactionManager, never()).rollback();
    }

    @Test
    void getUserAddresses_shouldReturnEmptyListWhenNoAddresses() {

        Long userId = 1L;
        User user = createUser(userId, true);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(addressDAO.findByUserId(session, userId))
                .thenReturn(List.of());

        List<Address> result =
                addressService.getUserAddresses(userId);

        assertTrue(result.isEmpty());

        verify(transactionManager).commit();
    }

    // ---------------------------------------------------------
    // GET SINGLE ADDRESS
    // ---------------------------------------------------------

    @Test
    void getAddress_shouldReturnAddress() {

        Long userId = 1L;
        Long addressId = 10L;

        User user = createUser(userId, true);
        Address address = createAddress();
        address.setId(addressId);
        address.setUser(user);

        when(addressDAO.findById(session, addressId))
                .thenReturn(address);

        Address result =
                addressService.getAddress(userId, addressId);

        assertSame(address, result);

        verify(addressDAO).findById(session, addressId);
        verify(transactionManager).commit();
    }

    @Test
    void getAddress_shouldFailWhenAddressDoesNotExist() {

        Long userId = 1L;
        Long addressId = 10L;

        when(addressDAO.findById(session, addressId))
                .thenReturn(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.getAddress(userId, addressId)
                );

        assertEquals("Address not found", exception.getMessage());

        verify(transactionManager).rollback();
        verify(transactionManager, never()).commit();
    }

    @Test
    void getAddress_shouldPreventAccessToAnotherUsersAddress() {

        Long userId = 1L;
        Long anotherUserId = 2L;
        Long addressId = 10L;

        User anotherUser = createUser(anotherUserId, true);

        Address address = createAddress();
        address.setId(addressId);
        address.setUser(anotherUser);

        when(addressDAO.findById(session, addressId))
                .thenReturn(address);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.getAddress(userId, addressId)
                );

        assertEquals("Address not found", exception.getMessage());

        verify(transactionManager).rollback();
        verify(transactionManager, never()).commit();
    }

    // ---------------------------------------------------------
    // UPDATE ADDRESS
    // ---------------------------------------------------------

    @Test
    void updateAddress_shouldUpdateAddress() {

        Long userId = 1L;
        Long addressId = 10L;

        User user = createUser(userId, true);

        Address existingAddress = createAddress();
        existingAddress.setId(addressId);
        existingAddress.setUser(user);

        Address updatedAddress = createAddress();
        updatedAddress.setFullName("Updated Name");
        updatedAddress.setPhone("9999999999");
        updatedAddress.setAddressLine("Updated Street");
        updatedAddress.setCity("Jaipur");
        updatedAddress.setState("Rajasthan");
        updatedAddress.setPincode("302001");

        when(addressDAO.findById(session, addressId))
                .thenReturn(existingAddress);

        addressService.updateAddress(
                userId,
                addressId,
                updatedAddress
        );

        assertEquals("Updated Name",
                existingAddress.getFullName());

        assertEquals("9999999999",
                existingAddress.getPhone());

        assertEquals("Updated Street",
                existingAddress.getAddressLine());

        assertEquals("Jaipur",
                existingAddress.getCity());

        assertEquals("Rajasthan",
                existingAddress.getState());

        assertEquals("302001",
                existingAddress.getPincode());

        verify(addressDAO).update(session, existingAddress);
        verify(transactionManager).commit();
        verify(transactionManager, never()).rollback();
    }

    @Test
    void updateAddress_shouldFailWhenAddressDoesNotExist() {

        Long userId = 1L;
        Long addressId = 10L;

        Address updatedAddress = createAddress();

        when(addressDAO.findById(session, addressId))
                .thenReturn(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.updateAddress(
                                userId,
                                addressId,
                                updatedAddress
                        )
                );

        assertEquals("Address not found", exception.getMessage());

        verify(addressDAO, never()).update(any(), any());
        verify(transactionManager).rollback();
        verify(transactionManager, never()).commit();
    }

    @Test
    void updateAddress_shouldPreventUpdatingAnotherUsersAddress() {

        Long userId = 1L;
        Long anotherUserId = 2L;
        Long addressId = 10L;

        User anotherUser = createUser(anotherUserId, true);

        Address existingAddress = createAddress();
        existingAddress.setId(addressId);
        existingAddress.setUser(anotherUser);

        Address updatedAddress = createAddress();

        when(addressDAO.findById(session, addressId))
                .thenReturn(existingAddress);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.updateAddress(
                                userId,
                                addressId,
                                updatedAddress
                        )
                );

        assertEquals("Address not found", exception.getMessage());

        verify(addressDAO, never()).update(any(), any());
        verify(transactionManager).rollback();
    }

    // ---------------------------------------------------------
    // DELETE ADDRESS
    // ---------------------------------------------------------

    @Test
    void deleteAddress_shouldDeleteAddress() {

        Long userId = 1L;
        Long addressId = 10L;

        User user = createUser(userId, true);

        Address address = createAddress();
        address.setId(addressId);
        address.setUser(user);

        when(addressDAO.findById(session, addressId))
                .thenReturn(address);

        addressService.deleteAddress(userId, addressId);

        verify(addressDAO).delete(session, address);
        verify(transactionManager).commit();
        verify(transactionManager, never()).rollback();
    }

    @Test
    void deleteAddress_shouldFailWhenAddressDoesNotExist() {

        Long userId = 1L;
        Long addressId = 10L;

        when(addressDAO.findById(session, addressId))
                .thenReturn(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.deleteAddress(
                                userId,
                                addressId
                        )
                );

        assertEquals("Address not found", exception.getMessage());

        verify(addressDAO, never()).delete(any(), any());
        verify(transactionManager).rollback();
        verify(transactionManager, never()).commit();
    }

    @Test
    void deleteAddress_shouldPreventDeletingAnotherUsersAddress() {

        Long userId = 1L;
        Long anotherUserId = 2L;
        Long addressId = 10L;

        User anotherUser = createUser(anotherUserId, true);

        Address address = createAddress();
        address.setId(addressId);
        address.setUser(anotherUser);

        when(addressDAO.findById(session, addressId))
                .thenReturn(address);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.deleteAddress(
                                userId,
                                addressId
                        )
                );

        assertEquals("Address not found", exception.getMessage());

        verify(addressDAO, never()).delete(any(), any());
        verify(transactionManager).rollback();
    }

    // ---------------------------------------------------------
    // VALIDATION
    // ---------------------------------------------------------

    @Test
    void addAddress_shouldRejectNullUserId() {

        Address address = createAddress();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.addAddress(null, address)
                );

        assertEquals("Invalid user ID", exception.getMessage());

        verifyNoInteractions(transactionManagerFactory);
    }

    @Test
    void addAddress_shouldRejectInvalidUserId() {

        Address address = createAddress();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.addAddress(0L, address)
                );

        assertEquals("Invalid user ID", exception.getMessage());

        verifyNoInteractions(transactionManagerFactory);
    }

    @Test
    void getAddress_shouldRejectInvalidAddressId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.getAddress(1L, 0L)
                );

        assertEquals("Invalid address ID", exception.getMessage());

        verifyNoInteractions(transactionManagerFactory);
    }

    @Test
    void addAddress_shouldRejectNullAddress() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.addAddress(1L, null)
                );

        assertEquals("Address cannot be null", exception.getMessage());

        verifyNoInteractions(transactionManagerFactory);
    }

    @Test
    void addAddress_shouldRejectBlankFullName() {

        Address address = createAddress();
        address.setFullName("   ");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.addAddress(1L, address)
                );

        assertEquals("Full name is required", exception.getMessage());
    }

    @Test
    void addAddress_shouldRejectBlankPhone() {

        Address address = createAddress();
        address.setPhone(" ");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.addAddress(1L, address)
                );

        assertEquals("Phone is required", exception.getMessage());
    }

    @Test
    void addAddress_shouldRejectBlankAddressLine() {

        Address address = createAddress();
        address.setAddressLine("");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.addAddress(1L, address)
                );

        assertEquals("Address line is required", exception.getMessage());
    }

    @Test
    void addAddress_shouldRejectBlankCity() {

        Address address = createAddress();
        address.setCity(" ");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.addAddress(1L, address)
                );

        assertEquals("City is required", exception.getMessage());
    }

    @Test
    void addAddress_shouldRejectBlankState() {

        Address address = createAddress();
        address.setState("");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.addAddress(1L, address)
                );

        assertEquals("State is required", exception.getMessage());
    }

    @Test
    void addAddress_shouldRejectBlankPincode() {

        Address address = createAddress();
        address.setPincode(" ");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> addressService.addAddress(1L, address)
                );

        assertEquals("Pincode is required", exception.getMessage());
    }

    // ---------------------------------------------------------
    // TRANSACTION ROLLBACK
    // ---------------------------------------------------------

    @Test
    void addAddress_shouldRollbackWhenDAOFails() {

        Long userId = 1L;

        User user = createUser(userId, true);
        Address address = createAddress();

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        doThrow(new RuntimeException("Database error"))
                .when(addressDAO)
                .save(session, address);

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> addressService.addAddress(userId, address)
                );

        assertEquals("Database error", exception.getMessage());

        verify(transactionManager).rollback();
        verify(transactionManager, never()).commit();
    }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    private User createUser(Long id, boolean active) {

        User user = new User();

        user.setId(id);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setPassword("hashed-password");
        user.setRole(com.cartly.entity.UserRole.CUSTOMER);
        user.setActive(active);

        return user;
    }

    private Address createAddress() {

        Address address = new Address();

        address.setFullName("Test User");
        address.setPhone("9876543210");
        address.setAddressLine("123 Main Street");
        address.setCity("Bikaner");
        address.setState("Rajasthan");
        address.setPincode("334001");

        return address;
    }
}