package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.CustomerAddress;
import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.services.AddressService;
import com.bookstore.backend.services.UserContextService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final AddressService addressService;
    private final UserContextService userContextService;

    public AddressController(AddressService addressService, UserContextService userContextService) {
        this.addressService = addressService;
        this.userContextService = userContextService;
    }

    /**
     * Creates a new address record for the authenticated customer.
     */
    @PostMapping
    public ResponseEntity<DataResponse<Void>> addAddress(@RequestBody CustomerAddress customerAddress) {
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
     */
    @GetMapping
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
     * Retrieves details of a specific address index location.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DataResponse<CustomerAddress>> getAddressById(@PathVariable Long id) {
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
     * Modifies data configurations of a specific address entry securely.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DataResponse<Void>> updateAddress(
            @PathVariable Long id, 
            @RequestBody CustomerAddress customerAddress) {
        
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
     * Completely removes a targeted address entity out of the customer profile.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<DataResponse<Void>> deleteAddress(@PathVariable Long id) {
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
