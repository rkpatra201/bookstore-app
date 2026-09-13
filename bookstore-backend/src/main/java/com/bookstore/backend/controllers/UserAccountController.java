package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.dtos.LoginRequest;
import com.bookstore.backend.dtos.LoginResponse;
import com.bookstore.backend.dtos.UserAccount;
import com.bookstore.backend.services.UserAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/user-accounts")
@Tag(name = "User Account Management", description = "APIs for managing user accounts")
public class UserAccountController {

    private final UserAccountService userAccountService;

    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

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
    public ResponseEntity<DataResponse<UserAccount>> registerUserAccount(@RequestBody UserAccount userAccount) {
        UserAccount createdAccount = userAccountService.createUserAccount(userAccount);

        DataResponse<UserAccount> response = new DataResponse<>(
                true,
                "User account registered successfully",
                createdAccount
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

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
    public ResponseEntity<DataResponse<LoginResponse>> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = userAccountService.login(loginRequest);

        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, loginResponse.getToken());

        DataResponse<LoginResponse> response = new DataResponse<>(
                true,
                "Login successful",
                loginResponse
        );
        return ResponseEntity.status(HttpStatus.OK).headers(headers).body(response);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user account by ID", description = "Fetches user account details using the unique user ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User account found successfully",
                    content = @Content(schema = @Schema(implementation = UserAccount.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User account not found",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<UserAccount>> getUserByUserId(
            @Parameter(description = "User ID to retrieve", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String userId) {

        UserAccount userAccount = userAccountService.findByUserId(userId);

        DataResponse<UserAccount> response = new DataResponse<>(
                true,
                "User account retrieved successfully",
                userAccount
        );
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
