package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.AuthorEntity;
import com.bookstore.backend.entities.BookEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class BookJdbcRepository {

    private final JdbcTemplate jdbcTemplate;

    // Spring injects JdbcTemplate automatically via constructor
    public BookJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<BookEntity> findAllBooksWithAuthors() {
        String sql = """
                SELECT 
                    b.id AS book_id, b.title, b.price, b.stock_qty,
                    a.id AS author_id, a.author_name, a.author_code
                FROM book b
                LEFT JOIN book_author ba ON b.id = ba.book_id
                LEFT JOIN author a ON ba.author_id = a.id
                """;

        // ResultSetExtractor collects multiple rows into a structured Map, then we convert values to a List
        return jdbcTemplate.query(sql, new ResultSetExtractor<List<BookEntity>>() {
            @Override
            public List<BookEntity> extractData(ResultSet rs) throws SQLException {
                // LinkedHashMap preserves the database order of books
                Map<Integer, BookEntity> bookMap = new LinkedHashMap<>();

                while (rs.next()) {
                    int bookId = rs.getInt("book_id");

                    // 1. If we haven't seen this book yet, map its core fields and add to map
                    BookEntity book = bookMap.get(bookId);
                    if (book == null) {
                        book = new BookEntity();
                        book.setId(bookId);
                        book.setTitle(rs.getString("title"));
                        book.setPrice(rs.getFloat("price"));
                        book.setStockQty(rs.getInt("stock_qty"));
                        book.setAuthors(new ArrayList<>()); // Initialize empty author list

                        bookMap.put(bookId, book);
                    }

                    // 2. If the row contains an author, map it and attach it to the book
                    int authorId = rs.getInt("author_id");
                    if (authorId > 0) { // Check if author exists (handles LEFT JOIN returning nulls)
                        AuthorEntity author = new AuthorEntity(authorId, rs.getString("author_name"), rs.getString("author_code"));
                        book.getAuthors().add(author);
                    }
                }

                return new ArrayList<>(bookMap.values());
            }
        });
    }
}
