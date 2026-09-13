set MODE MySQL;
-- 1. Create the Author table
DROP TABLE IF EXISTS author;

CREATE TABLE author (
    id INT AUTO_INCREMENT PRIMARY KEY,
    author_name VARCHAR(255) NOT NULL,
    author_code VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Create the Book table
DROP TABLE IF EXISTS book;

CREATE TABLE book (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL, -- DECIMAL is preferred over FLOAT for financial/price data
    stock_qty INT NOT NULL DEFAULT 0
);

-- 3. Create the Join Table (Book-Author mapping)
-- This maps the List<Author> inside your Book entity
DROP TABLE IF EXISTS book_author;

CREATE TABLE book_author (
    book_id INT NOT NULL,
    author_id INT NOT NULL,
    -- If using Spring Data JDBC List, an explicit ordering/key column is required:
    author_key INT NOT NULL,
    PRIMARY KEY (book_id, author_key),
    FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES author(id) ON DELETE CASCADE
);


-- Drops the table if it already exists to maintain a clean slate during development iterations
DROP TABLE IF EXISTS cart_line_items;

-- THE DYNAMIC SHOPPING CART TABLE
-- Maps to your CartLineItemEntity. Items are grouped directly by user_id.
CREATE TABLE cart_line_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    item_id INT NOT NULL,              -- Generic catalog item pointer (decoupled from 'book_id')
    quantity INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Crucial Constraint: Ensures a user can only have one row per generic item in their cart.
    -- If they add the same item again, your Repository UPSERT logic safely increments the quantity.
    CONSTRAINT unique_user_item UNIQUE (user_id, item_id)
);

-- PERFORMANCE INDEX
-- Speeds up database lookup speeds when retrieving or updating a specific user's cart rows
CREATE INDEX idx_cart_user ON cart_line_items(user_id);

DROP TABLE IF EXISTS customer_addresses;

CREATE TABLE customer_addresses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    alias VARCHAR(50), -- Optional alias (e.g., 'Home', 'Work', 'Office')
    recipient_name VARCHAR(100) NOT NULL,
    address_line1 VARCHAR(150) NOT NULL,
    address_line2 VARCHAR(150),
    city VARCHAR(50) NOT NULL,
    state VARCHAR(50) NOT NULL,
    postal_code VARCHAR(20) NOT NULL,
    country VARCHAR(50) NOT NULL,
    phone_number VARCHAR(20) NOT NULL,
    is_default BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_addresses_user_id ON customer_addresses(user_id);


---

-- 1. Master Orders Table (With Frozen Address Snapshot)
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id VARCHAR(50) NOT NULL,
    shipping_address_snapshot TEXT NOT NULL, -- Frozen copy of address (e.g., JSON or formatted string)
    total_amount DOUBLE NOT NULL,
    order_status VARCHAR(50) NOT NULL DEFAULT 'RESERVED', -- RESERVED, AWAITING_PAYMENT, PAYMENT_SUCCESS, PAYMENT_ERROR, PAYMENT_TIMEOUT, CANCELLED, REFUND_INITIATED, REFUND_ERROR, REFUND_COMPLETE
    payment_method VARCHAR(20) NOT NULL, -- COD, UPI, CC, DC, BANK
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 2. Order Line Items Table (Historical Snapshot)
CREATE TABLE IF NOT EXISTS order_line_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    item_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    unit_price DOUBLE NOT NULL,
    quantity INT NOT NULL,
    sub_total DOUBLE NOT NULL,
    CONSTRAINT fk_line_item_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Optimization Indexes
CREATE INDEX IF NOT EXISTS idx_orders_user_id ON orders(user_id);
CREATE INDEX IF NOT EXISTS idx_order_line_items_order_id ON order_line_items(order_id);

-- 1. Centralized Image Metadata Table
CREATE TABLE IF NOT EXISTS images (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    url VARCHAR(512) NOT NULL,               -- Stores the image URL string path safely
    alt_text VARCHAR(255),                   -- Description for frontend accessibility/SEO
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Book Images Link / Junction Table
CREATE TABLE IF NOT EXISTS book_images (
    book_id INT NOT NULL,
    image_id BIGINT NOT NULL,
    is_primary BOOLEAN DEFAULT FALSE,        -- True if this image is the main display front cover
    PRIMARY KEY (book_id, image_id),
    CONSTRAINT fk_book_images_book FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
    CONSTRAINT fk_book_images_image FOREIGN KEY (image_id) REFERENCES images(id) ON DELETE CASCADE
);

-- Optimization Index
CREATE INDEX IF NOT EXISTS idx_book_images_book_id ON book_images(book_id);

