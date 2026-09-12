package com.bookstore.backend.services;

import com.bookstore.backend.dtos.CustomerAddress;
import com.bookstore.backend.entities.CustomerAddressEntity;
import com.bookstore.backend.repositories.AddressRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    private static final String USER_ID = "user-abc-123";
    private static final Long ADDRESS_ID = 55L;

    @Test
    void addAddress_shouldMapDataAndInvokeRepositorySuccessfully() {
        // Arrange
        CustomerAddress dto = CustomerAddress.builder()
                .alias("Home")
                .addressLine1("123 Main St")
                .city("Bengaluru")
                .build();

        // Act
        addressService.addAddress(USER_ID, dto);

        // Assert: Capture the entity passed to repository to verify data conversion integrity
        ArgumentCaptor<CustomerAddressEntity> entityCaptor = ArgumentCaptor.forClass(CustomerAddressEntity.class);
        Mockito.verify(addressRepository, Mockito.times(1)).add(entityCaptor.capture());

        CustomerAddressEntity savedEntity = entityCaptor.getValue();
        Assertions.assertNotNull(savedEntity);
        Assertions.assertEquals(USER_ID, savedEntity.getUserId());
        Assertions.assertEquals("Home", savedEntity.getAlias());
        Assertions.assertEquals("123 Main St", savedEntity.getAddressLine1());
    }

    @Test
    void getAllAddresses_shouldReturnMappedList_whenCalled() {
        // Arrange
        CustomerAddressEntity entity = CustomerAddressEntity.builder()
                .id(ADDRESS_ID)
                .userId(USER_ID)
                .alias("Work")
                .build();
        Mockito.when(addressRepository.findAllByUserId(USER_ID)).thenReturn(List.of(entity));

        // Act
        List<CustomerAddress> result = addressService.getAllAddresses(USER_ID);

        // Assert
        Assertions.assertEquals(1, result.size());
        Assertions.assertEquals("Work", result.get(0).getAlias());
        Assertions.assertEquals(ADDRESS_ID, result.get(0).getId());
    }

    @Test
    void getAddressById_shouldReturnAddressDto_whenItemExists() {
        // Arrange
        CustomerAddressEntity entity = CustomerAddressEntity.builder()
                .id(ADDRESS_ID)
                .userId(USER_ID)
                .alias("Office")
                .build();
        Mockito.when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(entity);

        // Act
        CustomerAddress result = addressService.getAddressById(ADDRESS_ID, USER_ID);

        // Assert
        Assertions.assertNotNull(result);
        Assertions.assertEquals("Office", result.getAlias());
    }

    @Test
    void getAddressById_shouldThrowException_whenItemDoesNotExistOrOwnerMismatch() {
        // Arrange
        Mockito.when(addressRepository.findByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(null);

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> addressService.getAddressById(ADDRESS_ID, USER_ID)
        );

        Assertions.assertEquals("Address not found or access denied", exception.getMessage());
    }

    @Test
    void updateAddress_shouldCompleteSuccessfully_whenOwnerUpdatesValidAddress() {
        // Arrange
        CustomerAddress updateDto = CustomerAddress.builder().alias("New Alias").build();
        Mockito.when(addressRepository.updateByIdAndUserId(Mockito.any(CustomerAddressEntity.class))).thenReturn(true);

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> addressService.updateAddress(USER_ID, ADDRESS_ID, updateDto));

        ArgumentCaptor<CustomerAddressEntity> entityCaptor = ArgumentCaptor.forClass(CustomerAddressEntity.class);
        Mockito.verify(addressRepository, Mockito.times(1)).updateByIdAndUserId(entityCaptor.capture());
        
        CustomerAddressEntity captured = entityCaptor.getValue();
        Assertions.assertEquals(ADDRESS_ID, captured.getId());
        Assertions.assertEquals(USER_ID, captured.getUserId());
        Assertions.assertEquals("New Alias", captured.getAlias());
    }

    @Test
    void updateAddress_shouldThrowException_whenUpdateFailsInRepository() {
        // Arrange
        CustomerAddress updateDto = CustomerAddress.builder().alias("New Alias").build();
        Mockito.when(addressRepository.updateByIdAndUserId(Mockito.any(CustomerAddressEntity.class))).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> addressService.updateAddress(USER_ID, ADDRESS_ID, updateDto)
        );

        Assertions.assertEquals("Failed to update address. Address not found or access denied", exception.getMessage());
    }

    @Test
    void deleteAddress_shouldExecuteSuccessfully_whenItemIsDeleted() {
        // Arrange
        Mockito.when(addressRepository.deleteByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(true);

        // Act & Assert
        Assertions.assertDoesNotThrow(() -> addressService.deleteAddress(ADDRESS_ID, USER_ID));
        Mockito.verify(addressRepository, Mockito.times(1)).deleteByIdAndUserId(ADDRESS_ID, USER_ID);
    }

    @Test
    void deleteAddress_shouldThrowException_whenDeleteFailsInRepository() {
        // Arrange
        Mockito.when(addressRepository.deleteByIdAndUserId(ADDRESS_ID, USER_ID)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> addressService.deleteAddress(ADDRESS_ID, USER_ID)
        );

        Assertions.assertEquals("Failed to delete address. Address not found or access denied", exception.getMessage());
    }
}
