package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.services.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller for managing bookstore catalog operations.
 * <p>
 * Provides public endpoints for browsing books and viewing book details.
 * These endpoints do not require authentication and are accessible to all users.
 * </p>
 */
@RestController
@RequestMapping("api/books")
@Tag(name = "Book Management", description = "APIs for managing the bookstore inventory")
public class BookController {

    private BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /**
     * Retrieves all books available in the bookstore catalog.
     * <p>
     * Returns the complete list of books with details including title, author,
     * price, and stock quantity. This is a public endpoint accessible without authentication.
     * </p>
     *
     * @return ResponseEntity containing a list of all books and HTTP 200 OK
     */
    @GetMapping
    @Operation(summary = "Get all books", description = "Fetches a complete list of all books in the bookstore catalog")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved catalog",
                    content = @Content(schema = @Schema(implementation = List.class))
            )
    })
    public ResponseEntity<DataResponse<List<Book>>> getBooks() {
        List<Book> books = bookService.getBooks();
        DataResponse<List<Book>> response = new DataResponse<>(
                true,
                "Books fetched successfully",
                books
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Retrieves a specific book by its unique identifier.
     * <p>
     * Fetches detailed information about a single book including title, author,
     * description, price, and stock availability. This is a public endpoint
     * accessible without authentication.
     * </p>
     *
     * @param id the unique identifier of the book to retrieve
     * @return ResponseEntity containing the book details and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.BookException if book not found
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get book by ID", description = "Fetches details of a single book using its unique identification number")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Book found successfully",
                    content = @Content(schema = @Schema(implementation = Book.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Book not found with the provided ID",
                    content = @Content
            )
    })

    public ResponseEntity<DataResponse<Book>> getBookById(
            @Parameter(description = "ID of the book to retrieve", required = true, example = "123")
            @PathVariable int id) {

        Book book = bookService.getBookById(id);

        DataResponse<Book> successResponse = new DataResponse<>(
                true,
                "Book retrieved successfully",
                book
        );
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }
}
