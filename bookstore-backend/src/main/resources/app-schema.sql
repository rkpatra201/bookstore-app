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
