-- ==========================================
-- 1. INSERT AUTHORS
-- Note: 'id' is omitted as it is AUTO_INCREMENT (Generates IDs 1 to 4)
-- ==========================================
INSERT INTO author (author_name, author_code) VALUES ('J.K. Rowling', 'JKR001');       -- id: 1
INSERT INTO author (author_name, author_code) VALUES ('George R.R. Martin', 'GRM002'); -- id: 2
INSERT INTO author (author_name, author_code) VALUES ('Brandon Sanderson', 'BS003');   -- id: 3
INSERT INTO author (author_name, author_code) VALUES ('Gilbert Strang', 'GS004');      -- id: 4

-- ==========================================
-- 2. INSERT BOOKS
-- Note: 'id' is omitted as it is AUTO_INCREMENT (Generates IDs 1 to 4)
-- ==========================================
INSERT INTO book (title, price, stock_qty) VALUES ('Harry Potter and the Sorcerer''s Stone', 19.99, 4); -- id: 1
INSERT INTO book (title, price, stock_qty) VALUES ('A Game of Thrones', 24.99, 3);                     -- id: 2
INSERT INTO book (title, price, stock_qty) VALUES ('The Way of Kings', 29.99, 2);                      -- id: 3
INSERT INTO book (title, price, stock_qty) VALUES ('Introduction to Linear Algebra, Fifth Edition', 85.00, 5); -- id: 4

-- ==========================================
-- 3. LINK BOOKS TO AUTHORS (Join Table)
-- ==========================================
-- Book 1 (Harry Potter) -> Author 1 (J.K. Rowling)
INSERT INTO book_author (book_id, author_id, author_key) VALUES (1, 1, 0);

-- Book 2 (A Game of Thrones) -> Author 2 (G.R.R. Martin)
INSERT INTO book_author (book_id, author_id, author_key) VALUES (2, 2, 0);

-- Book 3 (The Way of Kings) -> Author 3 (B. Sanderson)
INSERT INTO book_author (book_id, author_id, author_key) VALUES (3, 3, 0);

-- Book 4 (Introduction to Linear Algebra) -> Author 4 (Gilbert Strang)
INSERT INTO book_author (book_id, author_id, author_key) VALUES (4, 4, 0);

-- ==========================================
-- 4. INSERT IMAGES METADATA
-- Note: 'id' is omitted as it is AUTO_INCREMENT (Generates IDs 1 to 4)
-- ==========================================
INSERT INTO images (url, alt_text)
VALUES ('https://math.mit.edu/~gs/linearalgebra/ila5/linearalgebra5_Front.jpg', 'Harry Potter and the Sorcerer''s Stone Front Cover'); -- id: 1

INSERT INTO images (url, alt_text)
VALUES ('https://math.mit.edu/~gs/linearalgebra/ila5/linearalgebra5_Front.jpg', 'A Game of Thrones Front Cover');                     -- id: 2

INSERT INTO images (url, alt_text)
VALUES ('https://math.mit.edu/~gs/linearalgebra/ila5/linearalgebra5_Front.jpg', 'The Way of Kings Front Cover');                      -- id: 3

INSERT INTO images (url, alt_text)
VALUES ('https://math.mit.edu/~gs/linearalgebra/ila5/linearalgebra5_Front.jpg', 'Introduction to Linear Algebra Fifth Edition Front Cover'); -- id: 4

-- ==========================================
-- 5. LINK BOOKS TO IMAGES (Junction Table)
-- ==========================================
-- Book 1 -> Image 1 (Primary Cover)
INSERT INTO book_images (book_id, image_id, is_primary) VALUES (1, 1, TRUE);

-- Book 2 -> Image 2 (Primary Cover)
INSERT INTO book_images (book_id, image_id, is_primary) VALUES (2, 2, TRUE);

-- Book 3 -> Image 3 (Primary Cover)
INSERT INTO book_images (book_id, image_id, is_primary) VALUES (3, 3, TRUE);

-- Book 4 -> Image 4 (Primary Cover)
INSERT INTO book_images (book_id, image_id, is_primary) VALUES (4, 4, TRUE);
