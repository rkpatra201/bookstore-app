package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.AuthorEntity;
import com.bookstore.backend.entities.BookEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
public class BookRepository {

    private final JdbcTemplate jdbcTemplate;

    // Spring injects JdbcTemplate automatically via constructor
    public BookRepository(JdbcTemplate jdbcTemplate) {
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

    public Optional<BookEntity> findById(Long id) {
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
            return Optional.of(book);

        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

}
