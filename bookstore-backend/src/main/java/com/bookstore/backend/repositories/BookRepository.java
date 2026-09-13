package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.AuthorEntity;
import com.bookstore.backend.entities.BookEntity;
import com.bookstore.backend.entities.ImageEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    // Spring injects JdbcTemplate automatically via constructor
    public BookRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BookEntity> findAllBooksWithAuthors() {
        // 1. First focused query: Fetch all master book records cleanly
        String bookSql = """
                SELECT id, title, price, stock_qty 
                FROM book
                """;

        List<BookEntity> books = jdbcTemplate.query(bookSql, (rs, rowNum) -> {
            BookEntity book = new BookEntity();
            book.setId(rs.getInt("id"));
            book.setTitle(rs.getString("title"));
            book.setPrice(rs.getFloat("price"));
            book.setStockQty(rs.getInt("stock_qty"));
            book.setAuthors(new ArrayList<>());
            book.setImages(new ArrayList<>());   // Prepare the image list placeholder
            return book;
        });

        if (books.isEmpty()) {
            return books;
        }

        // 2. Second focused query: Fetch and group all authors
        String authorSql = """
                SELECT ba.book_id, a.id AS author_id, a.author_name, a.author_code
                FROM book_author ba
                JOIN author a ON ba.author_id = a.id
                """;

        Map<Integer, List<AuthorEntity>> authorsMap = jdbcTemplate.query(authorSql, rs -> {
            Map<Integer, List<AuthorEntity>> map = new java.util.HashMap<>();
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                AuthorEntity author = new AuthorEntity(
                        rs.getInt("author_id"),
                        rs.getString("author_name"),
                        rs.getString("author_code")
                );
                map.computeIfAbsent(bookId, k -> new ArrayList<>()).add(author);
            }
            return map;
        });

        // 3. Third focused query: Fetch and group all images attached to books
        String imageSql = """
                SELECT bi.book_id, bi.is_primary, i.id AS image_id, i.url, i.alt_text
                FROM book_images bi
                JOIN images i ON bi.image_id = i.id
                """;

        Map<Integer, List<ImageEntity>> imagesMap = jdbcTemplate.query(imageSql, rs -> {
            Map<Integer, List<ImageEntity>> map = new java.util.HashMap<>();
            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                ImageEntity image = ImageEntity.builder()
                        .id(rs.getLong("image_id"))
                        .url(rs.getString("url"))
                        .altText(rs.getString("alt_text"))
                        .isPrimary(rs.getBoolean("is_primary")) // Map it here
                        .build();
                map.computeIfAbsent(bookId, k -> new ArrayList<>()).add(image);
            }
            return map;
        });


        // 4. Stitch data blocks together seamlessly in a single in-memory loop
        for (BookEntity book : books) {
            // Bind Authors
            if (authorsMap != null && authorsMap.containsKey(book.getId())) {
                book.setAuthors(authorsMap.get(book.getId()));
            }
            // Bind Images
            if (imagesMap != null && imagesMap.containsKey(book.getId())) {
                book.setImages(imagesMap.get(book.getId()));
            }
        }

        return books;
    }


    public Optional<BookEntity> findById(int id) {
        // 1. Fetch the base book fields
        String bookSql = "SELECT id, title, price, stock_qty FROM book WHERE id = ?";

        try {
            BookEntity book = jdbcTemplate.queryForObject(bookSql, (rs, rowNum) -> {
                BookEntity b = new BookEntity();
                b.setId(rs.getInt("id"));
                b.setTitle(rs.getString("title"));
                b.setPrice(rs.getFloat("price"));
                b.setStockQty(rs.getInt("stock_qty"));
                return b;
            }, id);

            // 2. Query and attach the associated authors
            String authorSql = """
                    SELECT a.id, a.author_name, a.author_code 
                    FROM author a
                    JOIN book_author ba ON a.id = ba.author_id
                    WHERE ba.book_id = ?
                    """;

            List<AuthorEntity> authors = jdbcTemplate.query(authorSql, (rs, rowNum) ->
                    new AuthorEntity(
                            rs.getInt("id"),
                            rs.getString("author_name"),
                            rs.getString("author_code")
                    ), id
            );

            book.setAuthors(authors);

            String imageSql = """
                    SELECT i.id, i.url, i.alt_text
                    FROM images i
                    JOIN book_images bi ON i.id = bi.image_id
                    WHERE bi.book_id = ?
                    """;
            List<ImageEntity> images = jdbcTemplate.query(imageSql, (rs, rowNum) ->
                    ImageEntity.builder()
                            .id(rs.getLong("id"))
                            .url(rs.getString("url"))
                            .altText(rs.getString("alt_text"))
                            .build(), id
            );
            book.setImages(images);

            return Optional.of(book);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
