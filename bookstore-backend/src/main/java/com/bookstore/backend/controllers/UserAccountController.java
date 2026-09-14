package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.dtos.LoginRequest;
import com.bookstore.backend.dtos.LoginResponse;
import com.bookstore.backend.dtos.UserAccount;
import com.bookstore.backend.services.UserAccountService;
import com.bookstore.backend.services.UserContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing user account operations.
 * <p>
 * Provides endpoints for user registration, login, logout, and profile retrieval.
 * Authentication is handled via JWT tokens stored in HttpOnly cookies.
 * Registration and login endpoints are public; other endpoints require authentication.
 * </p>
 */
@RestController
@RequestMapping("api/user-accounts")
@Tag(name = "User Account Management", description = "APIs for managing user accounts")
public class UserAccountController {

    private final UserAccountService userAccountService;
    private final UserContextService userContextService;

    public UserAccountController(UserAccountService userAccountService, UserContextService userContextService) {
        this.userAccountService = userAccountService;
        this.userContextService = userContextService;
    }

    /**
     * Registers a new user account in the system.
     * <p>
     * Creates a new user account with encrypted password and unique email.
     * Validates that required fields are provided and email is not already registered.
     * This is a public endpoint accessible without authentication.
     * </p>
     *
     * @param userAccount the user account details including email, password, firstName, lastName
     * @return ResponseEntity containing the created user account (without password) and HTTP 201 Created
     * @throws com.bookstore.backend.exceptions.UserAccountException if email already exists or validation fails
     */
    @PostMapping("/registration")
    @Operation(summary = "Register new user account", description = "Registers a new user account with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User account registered successfully",
                    content = @Content(schema = @Schema(implementation = UserAccount.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<UserAccount>> registerUserAccount(
            @Parameter(description = "User account registration details", required = true)
            @Valid @RequestBody UserAccount userAccount) {
        UserAccount createdAccount = userAccountService.createUserAccount(userAccount);

        DataResponse<UserAccount> response = new DataResponse<>(
                true,
                "User account registered successfully",
                createdAccount
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Authenticates a user and creates a session via HttpOnly cookie.
     * <p>
     * Validates user credentials (email and password) and returns user information.
     * On successful authentication, sets a JWT token in an HttpOnly cookie named "x-auth-cookie"
     * with 24-hour expiration. This is a public endpoint accessible without authentication.
     * </p>
     *
     * @param loginRequest the login credentials containing email and password
     * @return ResponseEntity containing user information (without password) and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.UserAccountException if user not found or credentials invalid
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user with email and password")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Login failed - invalid credentials",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<LoginResponse>> login(
            @Parameter(description = "Login credentials with email and password", required = true)
            @Valid @RequestBody LoginRequest loginRequest) {
        UserAccountService.LoginResult loginResult = userAccountService.login(loginRequest);

        // Create HttpOnly cookie for JWT token
        ResponseCookie jwtCookie = ResponseCookie.from("x-auth-cookie", loginResult.getToken())
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/")
                .maxAge(24 * 60 * 60) // 24 hours
                .sameSite("Lax")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        DataResponse<LoginResponse> response = new DataResponse<>(
                true,
                "Login successful",
                loginResult.getLoginResponse()
        );
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response);
    }

    /**
     * Retrieves the profile information of the currently authenticated user.
     * <p>
     * Fetches the user account details for the logged-in user based on the JWT token.
     * Returns user information without password field. Requires authentication.
     * </p>
     *
     * @return ResponseEntity containing the user account details and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.UserAccountException if user not found
     */
    @GetMapping("/me")
    @Operation(summary = "Get logged-in user profile", description = "Fetches profile details of the currently authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User account retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserAccount.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User account not found",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<UserAccount>> getLoggedInUserAccount() {

        String userId = userContextService.getUserContext().getUserId();
        UserAccount userAccount = userAccountService.findByUserId(userId);

        DataResponse<UserAccount> response = new DataResponse<>(
                true,
                "User account retrieved successfully",
                userAccount
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    /**
     * Logs out the currently authenticated user by clearing the session cookie.
     * <p>
     * Invalidates the authentication by clearing the HttpOnly cookie named "x-auth-cookie"
     * by setting its maxAge to 0. After logout, the user must log in again to access
     * protected endpoints. This endpoint does not require authentication.
     * </p>
     *
     * @return ResponseEntity with success message and HTTP 200 OK
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Clears the authentication cookie and logs out the user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Logout successful",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<Void>> logout() {
        // Create cookie with empty value and maxAge=0 to clear it
        ResponseCookie jwtCookie = ResponseCookie.from("x-auth-cookie", "")
                .httpOnly(true)
                .secure(false) // Set to true in production with HTTPS
                .path("/")
                .maxAge(0) // Expire immediately
                .sameSite("Lax")
                .build();

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());

        DataResponse<Void> response = new DataResponse<>(
                true,
                "Logout successful",
                null
        );
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response);
    }
}
