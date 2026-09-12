package com.cartly.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import com.cartly.dao.CartDAO;
import com.cartly.dao.CartItemDAO;
import com.cartly.dao.ProductDAO;
import com.cartly.dao.UserDAO;
import com.cartly.dto.CartItemResponse;
import com.cartly.dto.CartResponse;
import com.cartly.entity.Cart;
import com.cartly.entity.CartItem;
import com.cartly.entity.Product;
import com.cartly.entity.User;
import com.cartly.util.TransactionManager;
import com.cartly.util.TransactionManagerFactory;

public class CartService {

    private final CartDAO cartDAO;
    private final CartItemDAO cartItemDAO;
    private final ProductDAO productDAO;
    private final UserDAO userDAO;
    private final TransactionManagerFactory transactionManagerFactory;

    public CartService(
            CartDAO cartDAO,
            CartItemDAO cartItemDAO,
            ProductDAO productDAO,
            UserDAO userDAO,
            TransactionManagerFactory transactionManagerFactory) {

        this.cartDAO = cartDAO;
        this.cartItemDAO = cartItemDAO;
        this.productDAO = productDAO;
        this.userDAO = userDAO;
        this.transactionManagerFactory = transactionManagerFactory;
    }

    public void addItem(
            Long userId,
            Long productId,
            int quantity) {

        validateUserId(userId);
        validateProductId(productId);
        validateQuantity(quantity);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Cart cart =
                    cartDAO.findByUserId(session, userId);

            if (cart == null) {

                User user =
                        userDAO.findById(session, userId);

                if (user == null) {
                    throw new IllegalArgumentException(
                            "User not found");
                }

                cart = new Cart();
                cart.setUser(user);

                cartDAO.save(session, cart);

                // Cart ID is generated after persist/flush.
                session.flush();
            }

            Product product =
                    productDAO.findById(session, productId);

            validateProduct(product);

            if (quantity > product.getStock()) {
                throw new IllegalArgumentException(
                        "Insufficient stock");
            }

            CartItem cartItem =
                    cartItemDAO.findByCartAndProduct(
                            session,
                            cart.getId(),
                            productId);

            if (cartItem == null) {

                cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProduct(product);
                cartItem.setQuantity(quantity);

                cartItemDAO.save(session, cartItem);

            } else {

                int newQuantity =
                        cartItem.getQuantity() + quantity;

                if (newQuantity > product.getStock()) {
                    throw new IllegalArgumentException(
                            "Insufficient stock");
                }

                cartItem.setQuantity(newQuantity);
            }

            transactionManager.commit();

        } catch (Exception e) {
            transactionManager.rollback();
            throw e;
        }
    }

    public CartResponse getCart(Long userId) {

        validateUserId(userId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Cart cart =
                    cartDAO.findByUserId(session, userId);

            if (cart == null) {

                transactionManager.commit();

                CartResponse response = new CartResponse();
                response.setCartId(null);
                response.setItems(List.of());
                response.setTotal(BigDecimal.ZERO);

                return response;
            }

            List<CartItem> cartItems =
                    cartItemDAO.findByCartId(
                            session,
                            cart.getId());

            List<CartItemResponse> itemResponses =
                    new ArrayList<>();

            BigDecimal total = BigDecimal.ZERO;

            for (CartItem cartItem : cartItems) {

                Product product =
                        cartItem.getProduct();

                CartItemResponse itemResponse =
                        new CartItemResponse();

                itemResponse.setProductId(product.getId());
                itemResponse.setProductName(product.getName());
                itemResponse.setPrice(product.getPrice());
                itemResponse.setImageUrl(product.getImageUrl());
                itemResponse.setQuantity(
                        cartItem.getQuantity());

                itemResponses.add(itemResponse);

                BigDecimal itemTotal =
                        product.getPrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                cartItem.getQuantity()
                                        )
                                );

                total = total.add(itemTotal);
            }

            transactionManager.commit();

            CartResponse response = new CartResponse();
            response.setCartId(cart.getId());
            response.setItems(itemResponses);
            response.setTotal(total);

            return response;

        } catch (Exception e) {

            transactionManager.rollback();
            throw e;
        }
    }

    public void updateItemQuantity(
            Long userId,
            Long productId,
            int quantity) {

        validateUserId(userId);
        validateProductId(productId);
        validateQuantity(quantity);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Cart cart =
                    cartDAO.findByUserId(session, userId);

            if (cart == null) {
                throw new IllegalArgumentException(
                        "Cart not found");
            }

            Product product =
                    productDAO.findById(session, productId);

            validateProduct(product);

            if (quantity > product.getStock()) {
                throw new IllegalArgumentException(
                        "Insufficient stock");
            }

            CartItem cartItem =
                    cartItemDAO.findByCartAndProduct(
                            session,
                            cart.getId(),
                            productId);

            if (cartItem == null) {
                throw new IllegalArgumentException(
                        "Product is not in cart");
            }

            cartItem.setQuantity(quantity);

            transactionManager.commit();

        } catch (Exception e) {

            transactionManager.rollback();
            throw e;
        }
    }

    public void removeItem(
            Long userId,
            Long productId) {

        validateUserId(userId);
        validateProductId(productId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Cart cart =
                    cartDAO.findByUserId(session, userId);

            if (cart == null) {
                throw new IllegalArgumentException(
                        "Cart not found");
            }

            CartItem cartItem =
                    cartItemDAO.findByCartAndProduct(
                            session,
                            cart.getId(),
                            productId);

            if (cartItem == null) {
                throw new IllegalArgumentException(
                        "Product is not in cart");
            }

            cartItemDAO.delete(session, cartItem);

            transactionManager.commit();

        } catch (Exception e) {

            transactionManager.rollback();
            throw e;
        }
    }

    public void clearCart(Long userId) {

        validateUserId(userId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Cart cart =
                    cartDAO.findByUserId(session, userId);

            if (cart == null) {
                throw new IllegalArgumentException(
                        "Cart not found");
            }

            List<CartItem> cartItems =
                    cartItemDAO.findByCartId(
                            session,
                            cart.getId());

            for (CartItem cartItem : cartItems) {
                cartItemDAO.delete(session, cartItem);
            }

            transactionManager.commit();

        } catch (Exception e) {

            transactionManager.rollback();
            throw e;
        }
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException(
                    "User ID is required");
        }
    }

    private void validateProductId(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException(
                    "Product ID is required");
        }
    }

    private void validateQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero");
        }
    }

    private void validateProduct(Product product) {

        if (product == null) {
            throw new IllegalArgumentException(
                    "Product not found");
        }

        if (!product.isActive()) {
            throw new IllegalArgumentException(
                    "Product is inactive");
        }
    }
}