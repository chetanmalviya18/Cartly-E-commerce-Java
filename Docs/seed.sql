-- ============================================================
-- E-Commerce Seed Data
-- Database: ecommerce_db
-- ============================================================

USE ecommerce_db;


-- ============================================================
-- 1. USERS
-- ============================================================

INSERT INTO users
    (name, email, password, role, active)
VALUES
    ('Admin User', 'admin@ecommerce.com', 'admin123', 'ADMIN', TRUE),
    ('Chetan Malviya', 'chetan@example.com', 'password123', 'CUSTOMER', TRUE),
    ('Rahul Sharma', 'rahul@example.com', 'password123', 'CUSTOMER', TRUE),
    ('Priya Patel', 'priya@example.com', 'password123', 'CUSTOMER', TRUE);


-- ============================================================
-- 2. CATEGORIES
-- ============================================================

INSERT INTO categories
    (name, description)
VALUES
    ('Electronics', 'Electronic devices and accessories'),
    ('Clothing', 'Clothes and fashion products'),
    ('Books', 'Books and educational materials'),
    ('Home & Kitchen', 'Products for home and kitchen'),
    ('Sports', 'Sports and fitness products');


-- ============================================================
-- 3. PRODUCTS
-- ============================================================

INSERT INTO products
    (category_id, name, description, price, stock, image_url, active)
VALUES
    (
        1,
        'Laptop',
        '15.6 inch laptop suitable for work and entertainment',
        55000.00,
        10,
        'https://placehold.co/600x400?text=Laptop',
        TRUE
    ),
    (
        1,
        'Wireless Mouse',
        'Ergonomic wireless mouse with USB receiver',
        999.00,
        50,
        'https://placehold.co/600x400?text=Mouse',
        TRUE
    ),
    (
        1,
        'Mechanical Keyboard',
        'RGB mechanical keyboard with blue switches',
        2499.00,
        30,
        'https://placehold.co/600x400?text=Keyboard',
        TRUE
    ),
    (
        1,
        'USB-C Hub',
        'Multi-port USB-C hub for laptops',
        1799.00,
        25,
        'https://placehold.co/600x400?text=USB-C+Hub',
        TRUE
    ),
    (
        2,
        'Cotton T-Shirt',
        'Comfortable regular-fit cotton t-shirt',
        799.00,
        100,
        'https://placehold.co/600x400?text=T-Shirt',
        TRUE
    ),
    (
        2,
        'Denim Jeans',
        'Classic regular-fit denim jeans',
        1499.00,
        60,
        'https://placehold.co/600x400?text=Jeans',
        TRUE
    ),
    (
        3,
        'Clean Code',
        'A practical book about writing clean and maintainable code',
        599.00,
        40,
        'https://placehold.co/600x400?text=Clean+Code',
        TRUE
    ),
    (
        3,
        'Design Patterns',
        'Guide to reusable object-oriented software design',
        799.00,
        35,
        'https://placehold.co/600x400?text=Design+Patterns',
        TRUE
    ),
    (
        4,
        'Coffee Mug',
        'Ceramic coffee mug for everyday use',
        299.00,
        80,
        'https://placehold.co/600x400?text=Mug',
        TRUE
    ),
    (
        4,
        'Water Bottle',
        'Stainless steel reusable water bottle',
        699.00,
        70,
        'https://placehold.co/600x400?text=Water+Bottle',
        TRUE
    ),
    (
        5,
        'Yoga Mat',
        'Non-slip exercise and yoga mat',
        899.00,
        45,
        'https://placehold.co/600x400?text=Yoga+Mat',
        TRUE
    ),
    (
        5,
        'Running Shoes',
        'Lightweight running shoes for everyday training',
        2999.00,
        20,
        'https://placehold.co/600x400?text=Running+Shoes',
        TRUE
    );


-- ============================================================
-- 4. ADDRESSES
-- ============================================================

INSERT INTO addresses
    (
        user_id,
        full_name,
        phone,
        address_line,
        city,
        state,
        pincode
    )
VALUES
    (
        2,
        'Chetan Malviya',
        '9876543210',
        'Main Road',
        'Sumerpur',
        'Rajasthan',
        '306902'
    ),
    (
        2,
        'Chetan Malviya',
        '9876543210',
        'College Road',
        'Pali',
        'Rajasthan',
        '306401'
    ),
    (
        3,
        'Rahul Sharma',
        '9876543211',
        'MG Road',
        'Jaipur',
        'Rajasthan',
        '302001'
    ),
    (
        4,
        'Priya Patel',
        '9876543212',
        'Station Road',
        'Ahmedabad',
        'Gujarat',
        '380001'
    );


-- ============================================================
-- 5. CARTS
-- ============================================================

INSERT INTO carts
    (user_id)
VALUES
    (2),
    (3),
    (4);


-- ============================================================
-- 6. CART ITEMS
-- ============================================================

INSERT INTO cart_items
    (cart_id, product_id, quantity)
VALUES
    (1, 1, 1),   -- Laptop
    (1, 2, 2),   -- Wireless Mouse × 2
    (2, 5, 2),   -- T-Shirt × 2
    (2, 9, 1),   -- Coffee Mug
    (3, 11, 1),  -- Yoga Mat
    (3, 12, 1);  -- Running Shoes


-- ============================================================
-- 7. ORDERS
-- ============================================================

INSERT INTO orders
    (
        user_id,
        status,
        total_amount,
        shipping_name,
        shipping_phone,
        shipping_address,
        shipping_city,
        shipping_state,
        shipping_pincode
    )
VALUES
    (
        2,
        'DELIVERED',
        55999.00,
        'Chetan Malviya',
        '9876543210',
        'Main Road',
        'Sumerpur',
        'Rajasthan',
        '306902'
    ),
    (
        3,
        'CONFIRMED',
        2297.00,
        'Rahul Sharma',
        '9876543211',
        'MG Road',
        'Jaipur',
        'Rajasthan',
        '302001'
    );


-- ============================================================
-- 8. ORDER ITEMS
-- ============================================================

INSERT INTO order_items
    (order_id, product_id, quantity, price)
VALUES
    (1, 1, 1, 55000.00),  -- Laptop
    (1, 2, 1, 999.00),    -- Mouse

    (2, 5, 2, 799.00),    -- T-Shirt × 2
    (2, 9, 1, 699.00);    -- Water Bottle


-- ============================================================
-- 9. PAYMENTS
-- ============================================================

INSERT INTO payments
    (
        order_id,
        method,
        status,
        amount,
        transaction_reference
    )
VALUES
    (
        1,
        'UPI',
        'SUCCESS',
        55999.00,
        'TXN-100001'
    ),
    (
        2,
        'CARD',
        'SUCCESS',
        2297.00,
        'TXN-100002'
    );