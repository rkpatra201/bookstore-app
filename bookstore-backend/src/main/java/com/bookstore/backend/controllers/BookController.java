package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.Book;
import com.bookstore.backend.dtos.DataResponse; // Custom wrapper for standardized responses
import com.bookstore.backend.services.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/books")
@Tag(name = "Book Management", description = "APIs for managing the bookstore inventory")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * Retrieves all books available in the system.
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
     * Retrieves a specific book using its unique ID.
     *
     * @param id The unique identifier of the book
     * @return ResponseEntity containing the book details if found, or 404 Not Found
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
            @PathVariable Long id) {

        Book book = bookService.getBookById(id);

        if (book == null) {
            DataResponse<Book> errorResponse = new DataResponse<>(
                    false,
                    "Book not found with ID: " + id,
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }

        DataResponse<Book> successResponse = new DataResponse<>(
                true,
                "Book retrieved successfully",
                book
        );
        return ResponseEntity.status(HttpStatus.OK).body(successResponse);
    }
}
