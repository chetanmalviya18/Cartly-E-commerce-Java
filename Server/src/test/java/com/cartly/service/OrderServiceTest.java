package com.cartly.service;

import com.cartly.dao.AddressDAO;
import com.cartly.dao.CartDAO;
import com.cartly.dao.CartItemDAO;
import com.cartly.dao.OrderDAO;
import com.cartly.dao.OrderItemDAO;
import com.cartly.dao.ProductDAO;
import com.cartly.dao.UserDAO;
import com.cartly.entity.Address;
import com.cartly.entity.Cart;
import com.cartly.entity.CartItem;
import com.cartly.entity.Order;
import com.cartly.entity.OrderStatus;
import com.cartly.entity.Product;
import com.cartly.entity.User;
import com.cartly.util.TransactionManager;
import com.cartly.util.TransactionManagerFactory;
import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    private OrderDAO orderDAO;
    private OrderItemDAO orderItemDAO;
    private CartDAO cartDAO;
    private CartItemDAO cartItemDAO;
    private ProductDAO productDAO;
    private AddressDAO addressDAO;
    private UserDAO userDAO;
    private TransactionManagerFactory transactionManagerFactory;
    private TransactionManager transactionManager;
    private Session session;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderDAO = mock(OrderDAO.class);
        orderItemDAO = mock(OrderItemDAO.class);
        cartDAO = mock(CartDAO.class);
        cartItemDAO = mock(CartItemDAO.class);
        productDAO = mock(ProductDAO.class);
        addressDAO = mock(AddressDAO.class);
        userDAO = mock(UserDAO.class);

        transactionManagerFactory =
                mock(TransactionManagerFactory.class);

        transactionManager =
                mock(TransactionManager.class);

        session = mock(Session.class);

        when(transactionManagerFactory.create())
                .thenReturn(transactionManager);

        when(transactionManager.getSession())
                .thenReturn(session);

        orderService = new OrderService(
                orderDAO,
                orderItemDAO,
                cartDAO,
                cartItemDAO,
                productDAO,
                addressDAO,
                userDAO,
                transactionManagerFactory
        );
    }

    // =========================================================
    // PLACE ORDER
    // =========================================================

    @Test
    void placeOrder_shouldCreateOrderItemsReduceStockAndClearCart() {

        Long userId = 1L;
        Long addressId = 10L;

        User user = createUser(userId, true);
        Address address = createAddress(addressId, user);

        Cart cart = createCart(100L, user);

        Product product1 =
                createProduct(
                        101L,
                        "Laptop",
                        new BigDecimal("50000.00"),
                        10,
                        true
                );

        Product product2 =
                createProduct(
                        102L,
                        "Mouse",
                        new BigDecimal("1000.00"),
                        20,
                        true
                );

        CartItem item1 =
                createCartItem(201L, cart, product1, 2);

        CartItem item2 =
                createCartItem(202L, cart, product2, 3);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(addressDAO.findById(session, addressId))
                .thenReturn(address);

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(cartItemDAO.findByCartId(session, cart.getId()))
                .thenReturn(List.of(item1, item2));

        doAnswer(invocation -> {
            Order order = invocation.getArgument(1);
            order.setId(500L);
            return null;
        }).when(orderDAO).save(eq(session), any(Order.class));

        Order result =
                orderService.placeOrder(userId, addressId);

        assertNotNull(result);
        assertEquals(500L, result.getId());
        assertSame(user, result.getUser());
        assertEquals(OrderStatus.PLACED, result.getStatus());

        assertEquals(
                new BigDecimal("103000.00"),
                result.getTotalAmount()
        );

        // Address snapshot
        assertEquals(
                address.getFullName(),
                result.getShippingName()
        );

        assertEquals(
                address.getPhone(),
                result.getShippingPhone()
        );

        assertEquals(
                address.getAddressLine(),
                result.getShippingAddress()
        );

        assertEquals(
                address.getCity(),
                result.getShippingCity()
        );

        assertEquals(
                address.getState(),
                result.getShippingState()
        );

        assertEquals(
                address.getPincode(),
                result.getShippingPincode()
        );

        // Stock reduced
        assertEquals(8, product1.getStock());
        assertEquals(17, product2.getStock());

        verify(orderDAO).save(session, result);
        verify(session).flush();

        verify(orderItemDAO, times(2))
                .save(eq(session), any());

        verify(productDAO).update(session, product1);
        verify(productDAO).update(session, product2);

        verify(cartItemDAO).delete(session, item1);
        verify(cartItemDAO).delete(session, item2);

        verify(transactionManager).commit();
        verify(transactionManager, never()).rollback();
    }

    // =========================================================
    // EMPTY CART
    // =========================================================

    @Test
    void placeOrder_shouldFailWhenCartDoesNotExist() {

        Long userId = 1L;
        Long addressId = 10L;

        User user = createUser(userId, true);
        Address address = createAddress(addressId, user);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(addressDAO.findById(session, addressId))
                .thenReturn(address);

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                userId,
                                addressId
                        )
                );

        assertEquals("Cart is empty", exception.getMessage());

        verify(orderDAO, never()).save(any(), any());
        verify(transactionManager).rollback();
        verify(transactionManager, never()).commit();
    }

    @Test
    void placeOrder_shouldFailWhenCartHasNoItems() {

        Long userId = 1L;
        Long addressId = 10L;

        User user = createUser(userId, true);
        Address address = createAddress(addressId, user);
        Cart cart = createCart(100L, user);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(addressDAO.findById(session, addressId))
                .thenReturn(address);

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(cartItemDAO.findByCartId(session, cart.getId()))
                .thenReturn(List.of());

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                userId,
                                addressId
                        )
                );

        assertEquals("Cart is empty", exception.getMessage());

        verify(orderDAO, never()).save(any(), any());
        verify(transactionManager).rollback();
    }

    // =========================================================
    // USER VALIDATION
    // =========================================================

    @Test
    void placeOrder_shouldFailWhenUserDoesNotExist() {

        Long userId = 1L;
        Long addressId = 10L;

        when(userDAO.findById(session, userId))
                .thenReturn(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                userId,
                                addressId
                        )
                );

        assertEquals("User not found", exception.getMessage());

        verify(addressDAO, never()).findById(any(), any());
        verify(transactionManager).rollback();
    }

    @Test
    void placeOrder_shouldFailWhenUserIsInactive() {

        Long userId = 1L;
        Long addressId = 10L;

        User user = createUser(userId, false);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                userId,
                                addressId
                        )
                );

        assertEquals("User not found", exception.getMessage());

        verify(transactionManager).rollback();
    }

    // =========================================================
    // ADDRESS VALIDATION
    // =========================================================

    @Test
    void placeOrder_shouldFailWhenAddressDoesNotExist() {

        Long userId = 1L;
        Long addressId = 10L;

        User user = createUser(userId, true);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(addressDAO.findById(session, addressId))
                .thenReturn(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                userId,
                                addressId
                        )
                );

        assertEquals("Address not found", exception.getMessage());

        verify(cartDAO, never()).findByUserId(any(), any());
        verify(transactionManager).rollback();
    }

    @Test
    void placeOrder_shouldPreventUsingAnotherUsersAddress() {

        Long userId = 1L;
        Long anotherUserId = 2L;
        Long addressId = 10L;

        User user = createUser(userId, true);
        User anotherUser = createUser(anotherUserId, true);

        Address address =
                createAddress(addressId, anotherUser);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(addressDAO.findById(session, addressId))
                .thenReturn(address);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                userId,
                                addressId
                        )
                );

        assertEquals("Address not found", exception.getMessage());

        verify(cartDAO, never()).findByUserId(any(), any());
        verify(transactionManager).rollback();
    }

    // =========================================================
    // PRODUCT VALIDATION
    // =========================================================

    @Test
    void placeOrder_shouldFailWhenProductIsInactive() {

        Long userId = 1L;
        Long addressId = 10L;

        User user = createUser(userId, true);
        Address address = createAddress(addressId, user);
        Cart cart = createCart(100L, user);

        Product product =
                createProduct(
                        101L,
                        "Inactive Product",
                        new BigDecimal("1000.00"),
                        10,
                        false
                );

        CartItem cartItem =
                createCartItem(201L, cart, product, 1);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(addressDAO.findById(session, addressId))
                .thenReturn(address);

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(cartItemDAO.findByCartId(session, cart.getId()))
                .thenReturn(List.of(cartItem));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                userId,
                                addressId
                        )
                );

        assertEquals(
                "Product is no longer available: Inactive Product",
                exception.getMessage()
        );

        verify(orderDAO, never()).save(any(), any());
        verify(productDAO, never()).update(any(), any());

        verify(transactionManager).rollback();
        verify(transactionManager, never()).commit();
    }

    @Test
    void placeOrder_shouldFailWhenStockIsInsufficient() {

        Long userId = 1L;
        Long addressId = 10L;

        User user = createUser(userId, true);
        Address address = createAddress(addressId, user);
        Cart cart = createCart(100L, user);

        Product product =
                createProduct(
                        101L,
                        "Laptop",
                        new BigDecimal("50000.00"),
                        2,
                        true
                );

        CartItem cartItem =
                createCartItem(201L, cart, product, 5);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(addressDAO.findById(session, addressId))
                .thenReturn(address);

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(cartItemDAO.findByCartId(session, cart.getId()))
                .thenReturn(List.of(cartItem));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                userId,
                                addressId
                        )
                );

        assertEquals(
                "Insufficient stock for product: Laptop",
                exception.getMessage()
        );

        verify(orderDAO, never()).save(any(), any());
        verify(productDAO, never()).update(any(), any());

        verify(transactionManager).rollback();
    }

    // =========================================================
    // TRANSACTION ROLLBACK
    // =========================================================

    @Test
    void placeOrder_shouldRollbackWhenOrderCreationFails() {

        Long userId = 1L;
        Long addressId = 10L;

        User user = createUser(userId, true);
        Address address = createAddress(addressId, user);
        Cart cart = createCart(100L, user);

        Product product =
                createProduct(
                        101L,
                        "Laptop",
                        new BigDecimal("50000.00"),
                        10,
                        true
                );

        CartItem cartItem =
                createCartItem(201L, cart, product, 1);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(addressDAO.findById(session, addressId))
                .thenReturn(address);

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(cartItemDAO.findByCartId(session, cart.getId()))
                .thenReturn(List.of(cartItem));

        doThrow(new RuntimeException("Database error"))
                .when(orderDAO)
                .save(eq(session), any(Order.class));

        RuntimeException exception =
                assertThrows(
                        RuntimeException.class,
                        () -> orderService.placeOrder(
                                userId,
                                addressId
                        )
                );

        assertEquals("Database error", exception.getMessage());

        verify(transactionManager).rollback();
        verify(transactionManager, never()).commit();
    }

    // =========================================================
    // GET ORDER
    // =========================================================

    @Test
    void getOrder_shouldReturnUsersOrder() {

        Long userId = 1L;
        Long orderId = 500L;

        User user = createUser(userId, true);

        Order order = createOrder(orderId, user);

        when(orderDAO.findById(session, orderId))
                .thenReturn(order);

        Order result =
                orderService.getOrder(userId, orderId);

        assertSame(order, result);

        verify(orderDAO).findById(session, orderId);
        verify(transactionManager).commit();
        verify(transactionManager, never()).rollback();
    }

    @Test
    void getOrder_shouldPreventAccessToAnotherUsersOrder() {

        Long userId = 1L;
        Long anotherUserId = 2L;
        Long orderId = 500L;

        User anotherUser =
                createUser(anotherUserId, true);

        Order order =
                createOrder(orderId, anotherUser);

        when(orderDAO.findById(session, orderId))
                .thenReturn(order);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.getOrder(
                                userId,
                                orderId
                        )
                );

        assertEquals("Order not found", exception.getMessage());

        verify(transactionManager).rollback();
        verify(transactionManager, never()).commit();
    }

    @Test
    void getOrder_shouldFailWhenOrderDoesNotExist() {

        Long userId = 1L;
        Long orderId = 500L;

        when(orderDAO.findById(session, orderId))
                .thenReturn(null);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.getOrder(
                                userId,
                                orderId
                        )
                );

        assertEquals("Order not found", exception.getMessage());

        verify(transactionManager).rollback();
    }

    // =========================================================
    // GET USER ORDERS
    // =========================================================

    @Test
    void getUserOrders_shouldReturnOrders() {

        Long userId = 1L;

        User user = createUser(userId, true);

        Order order1 = createOrder(500L, user);
        Order order2 = createOrder(501L, user);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(orderDAO.findByUserId(session, userId))
                .thenReturn(List.of(order1, order2));

        List<Order> result =
                orderService.getUserOrders(userId);

        assertEquals(2, result.size());
        assertSame(order1, result.get(0));
        assertSame(order2, result.get(1));

        verify(orderDAO).findByUserId(session, userId);
        verify(transactionManager).commit();
    }

    @Test
    void getUserOrders_shouldReturnEmptyList() {

        Long userId = 1L;

        User user = createUser(userId, true);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(orderDAO.findByUserId(session, userId))
                .thenReturn(List.of());

        List<Order> result =
                orderService.getUserOrders(userId);

        assertTrue(result.isEmpty());

        verify(transactionManager).commit();
    }

    // =========================================================
    // INPUT VALIDATION
    // =========================================================

    @Test
    void placeOrder_shouldRejectNullUserId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                null,
                                10L
                        )
                );

        assertEquals("Invalid user ID", exception.getMessage());

        verifyNoInteractions(transactionManagerFactory);
    }

    @Test
    void placeOrder_shouldRejectInvalidUserId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                0L,
                                10L
                        )
                );

        assertEquals("Invalid user ID", exception.getMessage());

        verifyNoInteractions(transactionManagerFactory);
    }

    @Test
    void placeOrder_shouldRejectNullAddressId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                1L,
                                null
                        )
                );

        assertEquals("Invalid address ID", exception.getMessage());

        verifyNoInteractions(transactionManagerFactory);
    }

    @Test
    void placeOrder_shouldRejectInvalidAddressId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.placeOrder(
                                1L,
                                0L
                        )
                );

        assertEquals("Invalid address ID", exception.getMessage());

        verifyNoInteractions(transactionManagerFactory);
    }

    @Test
    void getOrder_shouldRejectInvalidOrderId() {

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> orderService.getOrder(
                                1L,
                                0L
                        )
                );

        assertEquals("Invalid order ID", exception.getMessage());

        verifyNoInteractions(transactionManagerFactory);
    }

    // =========================================================
    // HELPERS
    // =========================================================

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

    private Address createAddress(Long id, User user) {

        Address address = new Address();

        address.setId(id);
        address.setUser(user);
        address.setFullName("Test User");
        address.setPhone("9876543210");
        address.setAddressLine("123 Main Street");
        address.setCity("Bikaner");
        address.setState("Rajasthan");
        address.setPincode("334001");

        return address;
    }

    private Cart createCart(Long id, User user) {

        Cart cart = new Cart();

        cart.setId(id);
        cart.setUser(user);

        return cart;
    }

    private Product createProduct(
            Long id,
            String name,
            BigDecimal price,
            Integer stock,
            boolean active) {

        Product product = new Product();

        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setStock(stock);
        product.setActive(active);

        return product;
    }

    private CartItem createCartItem(
            Long id,
            Cart cart,
            Product product,
            Integer quantity) {

        CartItem cartItem = new CartItem();

        cartItem.setId(id);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);

        return cartItem;
    }

    private Order createOrder(Long id, User user) {

        Order order = new Order();

        order.setId(id);
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setTotalAmount(new BigDecimal("1000.00"));

        return order;
    }
}