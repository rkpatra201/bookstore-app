package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.CustomerAddress;
import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.services.AddressService;
import com.bookstore.backend.services.UserContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing customer delivery addresses.
 * <p>
 * Provides CRUD operations for user addresses used during checkout.
 * All operations are user-scoped and require authentication.
 * </p>
 */
@RestController
@RequestMapping("api/addresses")
@Tag(name = "Address Management", description = "APIs for managing customer delivery addresses")
public class AddressController {

    private final AddressService addressService;
    private final UserContextService userContextService;

    public AddressController(AddressService addressService, UserContextService userContextService) {
        this.addressService = addressService;
        this.userContextService = userContextService;
    }

    /**
     * Creates a new address record for the authenticated customer.
     * <p>
     * Validates the address details and associates it with the current user.
     * The address can be used during checkout for order delivery.
     * </p>
     *
     * @param customerAddress the address details to be saved
     * @return ResponseEntity with success message and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.AddressException if validation fails
     */
    @PostMapping
    @Operation(summary = "Add new address", description = "Creates a new delivery address for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Address added successfully",
                    content = @Content(schema = @Schema(implementation = DataResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid address data",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<Void>> addAddress(
            @Parameter(description = "Address details to be added", required = true)
            @Valid @RequestBody CustomerAddress customerAddress) {
        String userId = userContextService.getUserContext().getUserId();
        addressService.addAddress(userId, customerAddress);
        
        DataResponse<Void> response = new DataResponse<>(
                true, 
                "Address successfully added", 
                null
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Lists all saved addresses belonging to the authenticated customer.
     * <p>
     * Retrieves all delivery addresses associated with the current user.
     * Returns an empty list if no addresses are found.
     * </p>
     *
     * @return ResponseEntity containing list of addresses and HTTP 200 OK
     */
    @GetMapping
    @Operation(summary = "Get all addresses", description = "Retrieves all delivery addresses for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Addresses retrieved successfully",
                    content = @Content(schema = @Schema(implementation = DataResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<List<CustomerAddress>>> getAllAddresses() {
        String userId = userContextService.getUserContext().getUserId();
        List<CustomerAddress> addresses = addressService.getAllAddresses(userId);
        
        DataResponse<List<CustomerAddress>> response = new DataResponse<>(
                true, 
                "Addresses retrieved successfully", 
                addresses
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves details of a specific address by its unique identifier.
     * <p>
     * Fetches a single address belonging to the authenticated user.
     * Ensures the address belongs to the requesting user for security.
     * </p>
     *
     * @param id the unique identifier of the address to retrieve
     * @return ResponseEntity containing the address details and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.AddressException if address not found or doesn't belong to user
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get address by ID", description = "Retrieves a specific delivery address by its ID for the authenticated user")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Address retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CustomerAddress.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Address not found",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<CustomerAddress>> getAddressById(
            @Parameter(description = "Unique ID of the address to retrieve", required = true, example = "1")
            @PathVariable Long id) {
        String userId = userContextService.getUserContext().getUserId();
        CustomerAddress address = addressService.getAddressById(id, userId);
        
        DataResponse<CustomerAddress> response = new DataResponse<>(
                true, 
                "Address retrieved successfully", 
                address
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Updates an existing address with new details.
     * <p>
     * Modifies all fields of a specific address belonging to the authenticated user.
     * Validates the new address data before applying the update.
     * Ensures the address belongs to the requesting user for security.
     * </p>
     *
     * @param id the unique identifier of the address to update
     * @param customerAddress the updated address details
     * @return ResponseEntity with success message and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.AddressException if address not found, doesn't belong to user, or validation fails
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update address", description = "Updates an existing delivery address with new details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Address updated successfully",
                    content = @Content(schema = @Schema(implementation = DataResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid address data",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Address not found",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<Void>> updateAddress(
            @Parameter(description = "Unique ID of the address to update", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Updated address details", required = true)
            @Valid @RequestBody CustomerAddress customerAddress) {
        
        String userId = userContextService.getUserContext().getUserId();
        addressService.updateAddress(userId, id, customerAddress);
        
        DataResponse<Void> response = new DataResponse<>(
                true, 
                "Address updated successfully", 
                null
        );
        return ResponseEntity.ok(response);
    }

    /**
     * Deletes an address from the customer's saved addresses.
     * <p>
     * Permanently removes a specific address belonging to the authenticated user.
     * Ensures the address belongs to the requesting user for security.
     * </p>
     *
     * @param id the unique identifier of the address to delete
     * @return ResponseEntity with success message and HTTP 200 OK
     * @throws com.bookstore.backend.exceptions.AddressException if address not found or doesn't belong to user
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete address", description = "Removes a specific delivery address from the user's saved addresses")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Address deleted successfully",
                    content = @Content(schema = @Schema(implementation = DataResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "User not authenticated",
                    content = @Content
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Address not found",
                    content = @Content
            )
    })
    public ResponseEntity<DataResponse<Void>> deleteAddress(
            @Parameter(description = "Unique ID of the address to delete", required = true, example = "1")
            @PathVariable Long id) {
        String userId = userContextService.getUserContext().getUserId();
        addressService.deleteAddress(id, userId);
        
        DataResponse<Void> response = new DataResponse<>(
                true, 
                "Address deleted successfully", 
                null
        );
        return ResponseEntity.ok(response);
    }
}
