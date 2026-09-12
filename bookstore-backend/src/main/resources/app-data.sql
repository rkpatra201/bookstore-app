
-- 1. Insert Authors
-- Note: 'id' is omitted because it is AUTO_INCREMENT
INSERT INTO author (author_name, author_code) VALUES ('J.K. Rowling', 'JKR001');
INSERT INTO author (author_name, author_code) VALUES ('George R.R. Martin', 'GRM002');
INSERT INTO author (author_name, author_code) VALUES ('Brandon Sanderson', 'BS003');

-- 2. Insert Books
INSERT INTO book (title, price, stock_qty) VALUES ('Harry Potter and the Sorcerer''s Stone', 19.99, 120);
INSERT INTO book (title, price, stock_qty) VALUES ('A Game of Thrones', 24.99, 85);
INSERT INTO book (title, price, stock_qty) VALUES ('The Way of Kings', 29.99, 50);

-- 3. Link Books to Authors (Join Table)
-- Book 1 (Harry Potter) -> Author 1 (J.K. Rowling)
INSERT INTO book_author (book_id, author_id, author_key) VALUES (1, 1, 0);

-- Book 2 (A Game of Thrones) -> Author 2 (G.R.R. Martin)
INSERT INTO book_author (book_id, author_id, author_key) VALUES (2, 2, 0);

-- Book 3 (The Way of Kings) -> Author 3 (B. Sanderson)
INSERT INTO book_author (book_id, author_id, author_key) VALUES (3, 3, 0);
