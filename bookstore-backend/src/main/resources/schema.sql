-- 1. Create the Author table
CREATE TABLE author (
    id INT AUTO_INCREMENT PRIMARY KEY,
    author_name VARCHAR(255) NOT NULL,
    author_code VARCHAR(50) NOT NULL UNIQUE
);

-- 2. Create the Book table
CREATE TABLE book (
    id INT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    price DECIMAL(10, 2) NOT NULL, -- DECIMAL is preferred over FLOAT for financial/price data
    stock_qty INT NOT NULL DEFAULT 0
);

-- 3. Create the Join Table (Book-Author mapping)
-- This maps the List<Author> inside your Book entity
CREATE TABLE book_author (
    book_id INT NOT NULL,
    author_id INT NOT NULL,
    -- If using Spring Data JDBC List, an explicit ordering/key column is required:
    author_key INT NOT NULL,
    PRIMARY KEY (book_id, author_key),
    FOREIGN KEY (book_id) REFERENCES book(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES author(id) ON DELETE CASCADE
);
