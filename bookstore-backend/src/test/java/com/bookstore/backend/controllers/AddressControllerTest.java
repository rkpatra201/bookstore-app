package com.bookstore.backend.controllers;

import com.bookstore.backend.dtos.CustomerAddress;
import com.bookstore.backend.dtos.DataResponse;
import com.bookstore.backend.dtos.UserContext;
import com.bookstore.backend.services.AddressService;
import com.bookstore.backend.services.UserContextService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    @Mock
    private AddressService addressService;

    @Mock
    private UserContextService userContextService;

    @InjectMocks
    private AddressController addressController;

    private static final String USER_ID = "customer-user-111";
    private static final Long ADDRESS_ID = 55L;

    @BeforeEach
    void setUpUserContext() {
        // Enforce shared secure user context mocks across operational branches
        UserContext mockContext = Mockito.mock(UserContext.class);
        Mockito.when(mockContext.getUserId()).thenReturn(USER_ID);
        Mockito.when(userContextService.getUserContext()).thenReturn(mockContext);
    }

    @Test
    void addAddress_shouldReturnOkEnvelope_whenSuccessful() {
        // Arrange
        CustomerAddress payload = new CustomerAddress();
        Mockito.doNothing().when(addressService).addAddress(USER_ID, payload);

        // Act
        ResponseEntity<DataResponse<Void>> responseEntity = addressController.addAddress(payload);

        // Assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertTrue(responseEntity.getBody().isSuccess());
        Assertions.assertEquals("Address successfully added", responseEntity.getBody().getMessage());
        
        Mockito.verify(addressService, Mockito.times(1)).addAddress(USER_ID, payload);
    }

    @Test
    void getAllAddresses_shouldReturnListCollection_whenDataExists() {
        // Arrange
        CustomerAddress sample = new CustomerAddress();
        Mockito.when(addressService.getAllAddresses(USER_ID)).thenReturn(List.of(sample));

        // Act
        ResponseEntity<DataResponse<List<CustomerAddress>>> responseEntity = addressController.getAllAddresses();

        // Assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertEquals(1, responseEntity.getBody().getData().size());
        
        Mockito.verify(addressService, Mockito.times(1)).getAllAddresses(USER_ID);
    }

    @Test
    void getAddressById_shouldReturnTargetObject_whenAddressExists() {
        // Arrange
        CustomerAddress sample = new CustomerAddress();
        Mockito.when(addressService.getAddressById(ADDRESS_ID, USER_ID)).thenReturn(sample);

        // Act
        ResponseEntity<DataResponse<CustomerAddress>> responseEntity = addressController.getAddressById(ADDRESS_ID);

        // Assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertNotNull(responseEntity.getBody().getData());
        
        Mockito.verify(addressService, Mockito.times(1)).getAddressById(ADDRESS_ID, USER_ID);
    }

    @Test
    void updateAddress_shouldReturnSuccessEnvelope_whenServiceExecutes() {
        // Arrange
        CustomerAddress payload = new CustomerAddress();
        Mockito.doNothing().when(addressService).updateAddress(USER_ID, ADDRESS_ID, payload);

        // Act
        ResponseEntity<DataResponse<Void>> responseEntity = addressController.updateAddress(ADDRESS_ID, payload);

        // Assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertTrue(responseEntity.getBody().isSuccess());
        
        Mockito.verify(addressService, Mockito.times(1)).updateAddress(USER_ID, ADDRESS_ID, payload);
    }

    @Test
    void deleteAddress_shouldReturnSuccessEnvelope_whenServiceDeletes() {
        // Arrange
        Mockito.doNothing().when(addressService).deleteAddress(ADDRESS_ID, USER_ID);

        // Act
        ResponseEntity<DataResponse<Void>> responseEntity = addressController.deleteAddress(ADDRESS_ID);

        // Assert
        Assertions.assertNotNull(responseEntity);
        Assertions.assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Assertions.assertTrue(responseEntity.getBody().isSuccess());
        
        Mockito.verify(addressService, Mockito.times(1)).deleteAddress(ADDRESS_ID, USER_ID);
    }
}
