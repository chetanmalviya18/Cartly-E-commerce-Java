package com.cartly.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.hibernate.Session;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cartly.dao.CartDAO;
import com.cartly.dao.CartItemDAO;
import com.cartly.dao.ProductDAO;
import com.cartly.dao.UserDAO;
import com.cartly.dto.CartResponse;
import com.cartly.entity.Cart;
import com.cartly.entity.CartItem;
import com.cartly.entity.Product;
import com.cartly.entity.User;
import com.cartly.util.TransactionManager;
import com.cartly.util.TransactionManagerFactory;

class CartServiceTest {

    private CartDAO cartDAO;
    private CartItemDAO cartItemDAO;
    private ProductDAO productDAO;
    private UserDAO userDAO;

    private TransactionManager transactionManager;
    private TransactionManagerFactory transactionManagerFactory;
    private Session session;

    private CartService cartService;

    @BeforeEach
    void setUp() {

        cartDAO = mock(CartDAO.class);
        cartItemDAO = mock(CartItemDAO.class);
        productDAO = mock(ProductDAO.class);
        userDAO = mock(UserDAO.class);

        transactionManager = mock(TransactionManager.class);
        transactionManagerFactory =
                mock(TransactionManagerFactory.class);

        session = mock(Session.class);

        when(transactionManagerFactory.create())
                .thenReturn(transactionManager);

        when(transactionManager.getSession())
                .thenReturn(session);

        cartService = new CartService(
                cartDAO,
                cartItemDAO,
                productDAO,
                userDAO,
                transactionManagerFactory
        );
    }

    // =========================================================
    // ADD ITEM
    // =========================================================

    @Test
    void addItem_shouldCreateCartAndCartItem() {

        Long userId = 1L;
        Long productId = 10L;

        User user = createUser(userId);

        Product product = createProduct(
                productId,
                "iPhone",
                10,
                true,
                "79999.00"
        );

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(null);

        when(userDAO.findById(session, userId))
                .thenReturn(user);

        when(productDAO.findById(session, productId))
                .thenReturn(product);

        when(cartItemDAO.findByCartAndProduct(
                session,
                100L,
                productId))
                .thenReturn(null);

        doAnswer(invocation -> {

            Cart savedCart = invocation.getArgument(1);
            savedCart.setId(100L);

            return null;

        }).when(cartDAO).save(
                eq(session),
                any(Cart.class)
        );

        cartService.addItem(
                userId,
                productId,
                2
        );

        verify(cartDAO).save(
                eq(session),
                any(Cart.class)
        );

        verify(cartItemDAO).save(
                eq(session),
                argThat(cartItem ->
                        cartItem.getCart().getId().equals(100L)
                        && cartItem.getProduct().equals(product)
                        && cartItem.getQuantity() == 2
                )
        );

        verify(transactionManager).commit();

        verify(transactionManager, never()).rollback();
    }

    @Test
    void addItem_shouldUseExistingCart() {

        Long userId = 1L;
        Long productId = 10L;

        User user = createUser(userId);

        Cart cart = createCart(100L, user);

        Product product = createProduct(
                productId,
                "iPhone",
                10,
                true,
                "79999.00"
        );

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(productDAO.findById(session, productId))
                .thenReturn(product);

        when(cartItemDAO.findByCartAndProduct(
                session,
                100L,
                productId))
                .thenReturn(null);

        cartService.addItem(
                userId,
                productId,
                2
        );

        verify(cartDAO, never()).save(
                any(Session.class),
                any(Cart.class)
        );

        verify(cartItemDAO).save(
                eq(session),
                argThat(cartItem ->
                        cartItem.getCart() == cart
                        && cartItem.getProduct() == product
                        && cartItem.getQuantity() == 2
                )
        );

        verify(transactionManager).commit();
    }

    @Test
    void addItem_shouldIncreaseQuantityWhenProductAlreadyExists() {

        Long userId = 1L;
        Long productId = 10L;

        User user = createUser(userId);

        Cart cart = createCart(100L, user);

        Product product = createProduct(
                productId,
                "iPhone",
                10,
                true,
                "79999.00"
        );

        CartItem cartItem = createCartItem(
                500L,
                cart,
                product,
                2
        );

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(productDAO.findById(session, productId))
                .thenReturn(product);

        when(cartItemDAO.findByCartAndProduct(
                session,
                100L,
                productId))
                .thenReturn(cartItem);

        cartService.addItem(
                userId,
                productId,
                3
        );

        assertEquals(5, cartItem.getQuantity());

        verify(cartItemDAO, never()).save(
                any(Session.class),
                any(CartItem.class)
        );

        verify(transactionManager).commit();
    }

    @Test
    void addItem_shouldRejectInvalidQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addItem(
                        1L,
                        10L,
                        0
                )
        );

        verifyNoInteractions(transactionManagerFactory);
    }

    @Test
    void addItem_shouldRejectInactiveProduct() {

        Long userId = 1L;
        Long productId = 10L;

        User user = createUser(userId);

        Cart cart = createCart(100L, user);

        Product product = createProduct(
                productId,
                "Inactive Product",
                10,
                false,
                "100.00"
        );

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(productDAO.findById(session, productId))
                .thenReturn(product);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addItem(
                        userId,
                        productId,
                        2
                )
        );

        verify(transactionManager).rollback();

        verify(cartItemDAO, never()).save(
                any(Session.class),
                any(CartItem.class)
        );
    }

    @Test
    void addItem_shouldRejectWhenQuantityExceedsStock() {

        Long userId = 1L;
        Long productId = 10L;

        User user = createUser(userId);

        Cart cart = createCart(100L, user);

        Product product = createProduct(
                productId,
                "iPhone",
                2,
                true,
                "79999.00"
        );

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(productDAO.findById(session, productId))
                .thenReturn(product);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addItem(
                        userId,
                        productId,
                        5
                )
        );

        verify(transactionManager).rollback();

        verify(cartItemDAO, never()).save(
                any(Session.class),
                any(CartItem.class)
        );
    }

    // =========================================================
    // GET CART
    // =========================================================

    @Test
    void getCart_shouldReturnEmptyCartWhenCartDoesNotExist() {

        Long userId = 1L;

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(null);

        CartResponse response =
                cartService.getCart(userId);

        assertNull(response.getCartId());
        assertTrue(response.getItems().isEmpty());
        assertEquals(
                BigDecimal.ZERO,
                response.getTotal()
        );

        verify(transactionManager).commit();
    }

    @Test
    void getCart_shouldReturnItemsAndCalculateTotal() {

        Long userId = 1L;

        User user = createUser(userId);

        Cart cart = createCart(100L, user);

        Product product1 = createProduct(
                10L,
                "iPhone",
                10,
                true,
                "100.00"
        );

        Product product2 = createProduct(
                20L,
                "Keyboard",
                10,
                true,
                "50.00"
        );

        CartItem item1 = createCartItem(
                1L,
                cart,
                product1,
                2
        );

        CartItem item2 = createCartItem(
                2L,
                cart,
                product2,
                3
        );

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(cartItemDAO.findByCartId(
                session,
                100L))
                .thenReturn(List.of(item1, item2));

        CartResponse response =
                cartService.getCart(userId);

        assertEquals(
                100L,
                response.getCartId()
        );

        assertEquals(
                2,
                response.getItems().size()
        );

        // 100 * 2 + 50 * 3 = 350
        assertEquals(
                new BigDecimal("350.00"),
                response.getTotal()
        );

        verify(transactionManager).commit();
    }

    // =========================================================
    // UPDATE ITEM QUANTITY
    // =========================================================

    @Test
    void updateItemQuantity_shouldUpdateQuantity() {

        Long userId = 1L;
        Long productId = 10L;

        User user = createUser(userId);

        Cart cart = createCart(100L, user);

        Product product = createProduct(
                productId,
                "iPhone",
                10,
                true,
                "79999.00"
        );

        CartItem cartItem = createCartItem(
                500L,
                cart,
                product,
                2
        );

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(productDAO.findById(session, productId))
                .thenReturn(product);

        when(cartItemDAO.findByCartAndProduct(
                session,
                100L,
                productId))
                .thenReturn(cartItem);

        cartService.updateItemQuantity(
                userId,
                productId,
                5
        );

        assertEquals(
                5,
                cartItem.getQuantity()
        );

        verify(transactionManager).commit();
    }

    @Test
    void updateItemQuantity_shouldRejectQuantityAboveStock() {

        Long userId = 1L;
        Long productId = 10L;

        User user = createUser(userId);

        Cart cart = createCart(100L, user);

        Product product = createProduct(
                productId,
                "iPhone",
                3,
                true,
                "79999.00"
        );

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(productDAO.findById(session, productId))
                .thenReturn(product);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateItemQuantity(
                        userId,
                        productId,
                        5
                )
        );

        verify(transactionManager).rollback();
    }

    @Test
    void updateItemQuantity_shouldRejectProductNotInCart() {

        Long userId = 1L;
        Long productId = 10L;

        User user = createUser(userId);

        Cart cart = createCart(100L, user);

        Product product = createProduct(
                productId,
                "iPhone",
                10,
                true,
                "79999.00"
        );

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(productDAO.findById(session, productId))
                .thenReturn(product);

        when(cartItemDAO.findByCartAndProduct(
                session,
                100L,
                productId))
                .thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateItemQuantity(
                        userId,
                        productId,
                        5
                )
        );

        verify(transactionManager).rollback();
    }

    // =========================================================
    // REMOVE ITEM
    // =========================================================

    @Test
    void removeItem_shouldDeleteCartItem() {

        Long userId = 1L;
        Long productId = 10L;

        User user = createUser(userId);

        Cart cart = createCart(100L, user);

        Product product = createProduct(
                productId,
                "iPhone",
                10,
                true,
                "79999.00"
        );

        CartItem cartItem = createCartItem(
                500L,
                cart,
                product,
                2
        );

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(cartItemDAO.findByCartAndProduct(
                session,
                100L,
                productId))
                .thenReturn(cartItem);

        cartService.removeItem(
                userId,
                productId
        );

        verify(cartItemDAO).delete(
                session,
                cartItem
        );

        verify(transactionManager).commit();
    }

    @Test
    void removeItem_shouldRejectProductNotInCart() {

        Long userId = 1L;
        Long productId = 10L;

        User user = createUser(userId);

        Cart cart = createCart(100L, user);

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(cartItemDAO.findByCartAndProduct(
                session,
                100L,
                productId))
                .thenReturn(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.removeItem(
                        userId,
                        productId
                )
        );

        verify(transactionManager).rollback();

        verify(cartItemDAO, never()).delete(
                any(Session.class),
                any(CartItem.class)
        );
    }

    // =========================================================
    // CLEAR CART
    // =========================================================

    @Test
    void clearCart_shouldDeleteAllItems() {

        Long userId = 1L;

        User user = createUser(userId);

        Cart cart = createCart(100L, user);

        Product product1 = createProduct(
                10L,
                "iPhone",
                10,
                true,
                "100.00"
        );

        Product product2 = createProduct(
                20L,
                "Keyboard",
                10,
                true,
                "50.00"
        );

        CartItem item1 = createCartItem(
                1L,
                cart,
                product1,
                2
        );

        CartItem item2 = createCartItem(
                2L,
                cart,
                product2,
                1
        );

        when(cartDAO.findByUserId(session, userId))
                .thenReturn(cart);

        when(cartItemDAO.findByCartId(
                session,
                100L))
                .thenReturn(List.of(item1, item2));

        cartService.clearCart(userId);

        verify(cartItemDAO).delete(
                session,
                item1
        );

        verify(cartItemDAO).delete(
                session,
                item2
        );

        verify(transactionManager).commit();
    }

    // =========================================================
    // VALIDATION
    // =========================================================

    @Test
    void addItem_shouldRejectNullUserId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addItem(
                        null,
                        10L,
                        1
                )
        );
    }

    @Test
    void addItem_shouldRejectNullProductId() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addItem(
                        1L,
                        null,
                        1
                )
        );
    }

    @Test
    void updateItemQuantity_shouldRejectInvalidQuantity() {

        assertThrows(
                IllegalArgumentException.class,
                () -> cartService.updateItemQuantity(
                        1L,
                        10L,
                        0
                )
        );
    }

    // =========================================================
    // HELPER METHODS
    // =========================================================

    private User createUser(Long id) {

        User user = new User();
        user.setId(id);
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setActive(true);

        return user;
    }

    private Cart createCart(
            Long id,
            User user) {

        Cart cart = new Cart();

        cart.setId(id);
        cart.setUser(user);

        return cart;
    }

    private Product createProduct(
            Long id,
            String name,
            int stock,
            boolean active,
            String price) {

        Product product = new Product();

        product.setId(id);
        product.setName(name);
        product.setStock(stock);
        product.setActive(active);
        product.setPrice(new BigDecimal(price));

        return product;
    }

    private CartItem createCartItem(
            Long id,
            Cart cart,
            Product product,
            int quantity) {

        CartItem cartItem = new CartItem();

        cartItem.setId(id);
        cartItem.setCart(cart);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);

        return cartItem;
    }
}

