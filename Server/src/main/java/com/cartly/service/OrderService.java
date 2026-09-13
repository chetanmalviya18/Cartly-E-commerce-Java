package com.cartly.service;

import com.cartly.dao.AddressDAO;
import com.cartly.dao.CartDAO;
import com.cartly.dao.CartItemDAO;
import com.cartly.dao.OrderDAO;
import com.cartly.dao.OrderItemDAO;
import com.cartly.dao.ProductDAO;
import com.cartly.dao.UserDAO;
import com.cartly.dto.OrderResponse;
import com.cartly.entity.Address;
import com.cartly.entity.Cart;
import com.cartly.entity.CartItem;
import com.cartly.entity.Order;
import com.cartly.entity.OrderItem;
import com.cartly.entity.OrderStatus;
import com.cartly.entity.Product;
import com.cartly.entity.User;
import com.cartly.util.TransactionManager;
import com.cartly.util.TransactionManagerFactory;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.List;

public class OrderService {

    private final OrderDAO orderDAO;
    private final OrderItemDAO orderItemDAO;
    private final CartDAO cartDAO;
    private final CartItemDAO cartItemDAO;
    private final ProductDAO productDAO;
    private final AddressDAO addressDAO;
    private final UserDAO userDAO;
    private final TransactionManagerFactory transactionManagerFactory;

    public OrderService(
            OrderDAO orderDAO,
            OrderItemDAO orderItemDAO,
            CartDAO cartDAO,
            CartItemDAO cartItemDAO,
            ProductDAO productDAO,
            AddressDAO addressDAO,
            UserDAO userDAO,
            TransactionManagerFactory transactionManagerFactory) {

        this.orderDAO = orderDAO;
        this.orderItemDAO = orderItemDAO;
        this.cartDAO = cartDAO;
        this.cartItemDAO = cartItemDAO;
        this.productDAO = productDAO;
        this.addressDAO = addressDAO;
        this.userDAO = userDAO;
        this.transactionManagerFactory = transactionManagerFactory;
    }

    public Order placeOrder(Long userId, Long addressId) {

        validateUserId(userId);
        validateAddressId(addressId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            // 1. Validate user
            User user = userDAO.findById(session, userId);

            if (user == null || !user.isActive()) {
                throw new IllegalArgumentException("User not found");
            }

            // 2. Validate address belongs to user
            Address address =
                    addressDAO.findById(session, addressId);

            if (address == null ||
                    !address.getUser().getId().equals(userId)) {

                throw new IllegalArgumentException("Address not found");
            }

            // 3. Find user's cart
            Cart cart =
                    cartDAO.findByUserId(session, userId);

            if (cart == null) {
                throw new IllegalArgumentException("Cart is empty");
            }

            // 4. Get cart items
            List<CartItem> cartItems =
                    cartItemDAO.findByCartId(session, cart.getId());

            if (cartItems.isEmpty()) {
                throw new IllegalArgumentException("Cart is empty");
            }

            // 5. Validate stock and calculate total
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (CartItem cartItem : cartItems) {

                Product product = cartItem.getProduct();

                if (!product.isActive()) {
                    throw new IllegalArgumentException(
                            "Product is no longer available: "
                                    + product.getName()
                    );
                }

                if (cartItem.getQuantity() > product.getStock()) {
                    throw new IllegalArgumentException(
                            "Insufficient stock for product: "
                                    + product.getName()
                    );
                }

                BigDecimal itemTotal =
                        product.getPrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                cartItem.getQuantity()
                                        )
                                );

                totalAmount = totalAmount.add(itemTotal);
            }

            // 6. Create order
            Order order = new Order();

            order.setUser(user);
            order.setStatus(OrderStatus.PLACED);
            order.setTotalAmount(totalAmount);

            // Address snapshot
            order.setShippingName(address.getFullName());
            order.setShippingPhone(address.getPhone());
            order.setShippingAddress(address.getAddressLine());
            order.setShippingCity(address.getCity());
            order.setShippingState(address.getState());
            order.setShippingPincode(address.getPincode());

            orderDAO.save(session, order);

            // Flush so generated order ID is available
            session.flush();

            // 7. Create order items and reduce stock
            for (CartItem cartItem : cartItems) {

                Product product = cartItem.getProduct();

                OrderItem orderItem = new OrderItem();

                orderItem.setOrder(order);
                orderItem.setProduct(product);
                orderItem.setQuantity(cartItem.getQuantity());

                // Price snapshot
                orderItem.setPrice(product.getPrice());

                orderItemDAO.save(session, orderItem);

                // Reduce inventory
                product.setStock(
                        product.getStock()
                                - cartItem.getQuantity()
                );

                productDAO.update(session, product);
            }

            // 8. Clear cart
            for (CartItem cartItem : cartItems) {
                cartItemDAO.delete(session, cartItem);
            }

            // 9. Commit everything together
            transactionManager.commit();

            return order;

        } catch (RuntimeException e) {

            transactionManager.rollback();
            throw e;
        }
    }

    public Order getOrder(Long userId, Long orderId) {

        validateUserId(userId);
        validateOrderId(orderId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Order order =
                    orderDAO.findById(session, orderId);

            if (order == null ||
                    !order.getUser().getId().equals(userId)) {

                throw new IllegalArgumentException(
                        "Order not found"
                );
            }

            transactionManager.commit();

            return order;

        } catch (RuntimeException e) {

            transactionManager.rollback();
            throw e;
        }
    }

    public List<Order> getUserOrders(Long userId) {

        validateUserId(userId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            User user = userDAO.findById(session, userId);

            if (user == null || !user.isActive()) {
                throw new IllegalArgumentException(
                        "User not found"
                );
            }

            List<Order> orders =
                    orderDAO.findByUserId(session, userId);

            transactionManager.commit();

            return orders;

        } catch (RuntimeException e) {

            transactionManager.rollback();
            throw e;
        }
    }

    private void validateUserId(Long userId) {

        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid user ID"
            );
        }
    }

    private void validateAddressId(Long addressId) {

        if (addressId == null || addressId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid address ID"
            );
        }
    }

    private void validateOrderId(Long orderId) {

        if (orderId == null || orderId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid order ID"
            );
        }
    }
    
    public List<OrderItem> getOrderItems(Long userId, Long orderId) {

        validateUserId(userId);
        validateOrderId(orderId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Order order = orderDAO.findById(session, orderId);

            if (order == null ||
                    !order.getUser().getId().equals(userId)) {

                throw new IllegalArgumentException(
                        "Order not found"
                );
            }

            List<OrderItem> items =
                    orderItemDAO.findByOrderId(session, orderId);

            transactionManager.commit();

            return items;

        } catch (RuntimeException e) {

            transactionManager.rollback();
            throw e;
        }
    }
    
    public OrderResponse getOrderResponse(Long userId, Long orderId) {

        validateUserId(userId);
        validateOrderId(orderId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            Order order =
                    orderDAO.findById(session, orderId);

            if (order == null ||
                    !order.getUser().getId().equals(userId)) {

                throw new IllegalArgumentException(
                        "Order not found"
                );
            }

            List<OrderItem> orderItems =
                    orderItemDAO.findByOrderId(session, orderId);

            OrderResponse response =
                    new OrderResponse(order, orderItems);

            transactionManager.commit();

            return response;

        } catch (RuntimeException e) {

            transactionManager.rollback();
            throw e;
        }
    }
    
    public List<OrderResponse> getUserOrderResponses(Long userId) {

        validateUserId(userId);

        TransactionManager transactionManager =
                transactionManagerFactory.create();

        try {
            Session session = transactionManager.getSession();

            User user = userDAO.findById(session, userId);

            if (user == null || !user.isActive()) {
                throw new IllegalArgumentException(
                        "User not found"
                );
            }

            List<Order> orders =
                    orderDAO.findByUserId(session, userId);

            List<OrderResponse> responses = orders.stream()
                    .map(order -> {

                        List<OrderItem> items =
                                orderItemDAO.findByOrderId(
                                        session,
                                        order.getId()
                                );

                        return new OrderResponse(order, items);
                    })
                    .toList();

            transactionManager.commit();

            return responses;

        } catch (RuntimeException e) {

            transactionManager.rollback();
            throw e;
        }
    }
    
    public OrderResponse placeOrderResponse(
            Long userId,
            Long addressId) {

        Order order = placeOrder(userId, addressId);

        return getOrderResponse(userId, order.getId());
    }
}