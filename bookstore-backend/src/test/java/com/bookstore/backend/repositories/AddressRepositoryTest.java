package com.bookstore.backend.repositories;

import com.bookstore.backend.entities.CustomerAddressEntity;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

@JdbcTest
@ActiveProfiles("test")
@Import(AddressRepository.class)
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    private static final String USER_ID = "user-customer-777";
    private static final String ATTACKER_USER_ID = "user-malicious-999";

    @Test
    void addressLifecycle_shouldAddAndFindAddressCorrectly() {
        // Arrange
        CustomerAddressEntity newAddress = createSampleAddress(USER_ID, "Home");

        // Act: Save address record
        addressRepository.add(newAddress);

        // Fetch back using find all list to capture auto-generated database primary key ID
        List<CustomerAddressEntity> addressList = addressRepository.findAllByUserId(USER_ID);
        Assertions.assertEquals(1, addressList.size());
        Long savedId = addressList.get(0).getId();

        // Act: Find by ID and Owner Context
        CustomerAddressEntity foundAddress = addressRepository.findByIdAndUserId(savedId, USER_ID);

        // Assert
        Assertions.assertNotNull(foundAddress);
        Assertions.assertEquals("Home", foundAddress.getAlias());
        Assertions.assertEquals("123 Main St", foundAddress.getAddressLine1());
        Assertions.assertEquals("Bengaluru", foundAddress.getCity());
    }

    @Test
    void findByIdAndUserId_shouldReturnNull_whenQueriedByWrongUser() {
        // Arrange
        CustomerAddressEntity address = createSampleAddress(USER_ID, "Work");
        addressRepository.add(address);
        Long savedId = addressRepository.findAllByUserId(USER_ID).get(0).getId();

        // Act: Attempt to retrieve victim's address using attacker account
        CustomerAddressEntity vulnerableLookup = addressRepository.findByIdAndUserId(savedId, ATTACKER_USER_ID);

        // Assert: Security check holds
        Assertions.assertNull(vulnerableLookup);
    }

    @Test
    void updateByIdAndUserId_shouldModifyData_whenOwnerUpdates() {
        // Arrange
        CustomerAddressEntity address = createSampleAddress(USER_ID, "Office");
        addressRepository.add(address);
        CustomerAddressEntity savedInstance = addressRepository.findAllByUserId(USER_ID).get(0);

        // Modify a field value
        savedInstance.setAddressLine1("456 Corporate Towers");
        savedInstance.setAlias("Headquarters");

        // Act
        boolean isUpdated = addressRepository.updateByIdAndUserId(savedInstance);

        // Assert
        Assertions.assertTrue(isUpdated);
        CustomerAddressEntity verifiedRecord = addressRepository.findByIdAndUserId(savedInstance.getId(), USER_ID);
        Assertions.assertEquals("456 Corporate Towers", verifiedRecord.getAddressLine1());
        Assertions.assertEquals("Headquarters", verifiedRecord.getAlias());
    }

    @Test
    void updateByIdAndUserId_shouldReturnFalseAndNotModify_whenWrongUserAttemptsUpdate() {
        // Arrange
        CustomerAddressEntity address = createSampleAddress(USER_ID, "Office");
        addressRepository.add(address);
        CustomerAddressEntity savedInstance = addressRepository.findAllByUserId(USER_ID).get(0);

        // Malicious user attempts payload tampering targeting victim id
        CustomerAddressEntity maliciousPayload = createSampleAddress(ATTACKER_USER_ID, "Hacked Alias");
        maliciousPayload.setId(savedInstance.getId()); 

        // Act
        boolean isUpdated = addressRepository.updateByIdAndUserId(maliciousPayload);

        // Assert
        Assertions.assertFalse(isUpdated);
        // Verify original record is completely uncorrupted
        CustomerAddressEntity uncorruptedRecord = addressRepository.findByIdAndUserId(savedInstance.getId(), USER_ID);
        Assertions.assertEquals("Office", uncorruptedRecord.getAlias());
    }

    @Test
    void deleteByIdAndUserId_shouldRemoveRow_whenOwnerDeletes() {
        // Arrange
        CustomerAddressEntity address = createSampleAddress(USER_ID, "Temp Address");
        addressRepository.add(address);
        Long savedId = addressRepository.findAllByUserId(USER_ID).get(0).getId();

        // Act
        boolean isDeleted = addressRepository.deleteByIdAndUserId(savedId, USER_ID);

        // Assert
        Assertions.assertTrue(isDeleted);
        Assertions.assertTrue(addressRepository.findAllByUserId(USER_ID).isEmpty());
    }

    @Test
    void deleteByIdAndUserId_shouldReturnFalseAndKeepRow_whenWrongUserDeletes() {
        // Arrange
        CustomerAddressEntity address = createSampleAddress(USER_ID, "Protected Location");
        addressRepository.add(address);
        Long savedId = addressRepository.findAllByUserId(USER_ID).get(0).getId();

        // Act: Attacker attempts to delete victim's address ID
        boolean isDeleted = addressRepository.deleteByIdAndUserId(savedId, ATTACKER_USER_ID);

        // Assert
        Assertions.assertFalse(isDeleted);
        // Verify row is still intact for the rightful customer
        Assertions.assertEquals(1, addressRepository.findAllByUserId(USER_ID).size());
    }

    // Helper builder provider method
    private CustomerAddressEntity createSampleAddress(String ownerId, String targetAlias) {
        return CustomerAddressEntity.builder()
                .userId(ownerId)
                .alias(targetAlias)
                .recipientName("John Doe")
                .addressLine1("123 Main St")
                .city("Bengaluru")
                .state("Karnataka")
                .postalCode("560016")
                .country("India")
                .phoneNumber("+919876543210")
                .isDefault(false)
                .build();
    }
}
