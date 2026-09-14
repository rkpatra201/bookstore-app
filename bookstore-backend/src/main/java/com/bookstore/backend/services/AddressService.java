package com.bookstore.backend.services;

import com.bookstore.backend.dtos.CustomerAddress;
import com.bookstore.backend.entities.CustomerAddressEntity;
import com.bookstore.backend.enums.AddressError;
import com.bookstore.backend.exceptions.AddressException;
import com.bookstore.backend.mappers.AddressMapper;
import com.bookstore.backend.repositories.AddressRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper = AddressMapper.INSTANCE;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public void addAddress(String userId, CustomerAddress customerAddress) {
        log.info("Adding new address - User: {}, City: {}, Country: {}", userId, customerAddress.getCity(), customerAddress.getCountry());

        try {
            CustomerAddressEntity entity = addressMapper.toEntity(customerAddress);
            entity.setUserId(userId);
            addressRepository.add(entity);

            log.info("Successfully added address - User: {}", userId);
        } catch (Exception e) {
            log.error("Failed to add address - User: {}, Error: {}", userId, e.getMessage());
            throw e;
        }
    }

    public List<CustomerAddress> getAllAddresses(String userId) {
        log.debug("Fetching all addresses for user: {}", userId);
        List<CustomerAddressEntity> entities = addressRepository.findAllByUserId(userId);
        log.info("Addresses retrieved - User: {}, Count: {}", userId, entities.size());
        return addressMapper.toDtoList(entities);
    }

    public CustomerAddress getAddressById(Long id, String userId) {
        log.debug("Fetching address - AddressId: {}, User: {}", id, userId);
        CustomerAddressEntity entity = addressRepository.findByIdAndUserId(id, userId);
        if (entity == null) {
            log.warn("Address not found or access denied - AddressId: {}, User: {}", id, userId);
            throw new AddressException(AddressError.ADDRESS_NOT_FOUND);
        }
        log.info("Address retrieved - AddressId: {}, User: {}", id, userId);
        return addressMapper.toDto(entity);
    }

    public void updateAddress(String userId, Long id, CustomerAddress customerAddress) {
        log.info("Updating address - AddressId: {}, User: {}", id, userId);

        CustomerAddressEntity entity = addressMapper.toEntity(customerAddress);
        entity.setId(id);
        entity.setUserId(userId);

        boolean updated = addressRepository.updateByIdAndUserId(entity);
        if (!updated) {
            log.warn("Failed to update address - AddressId: {}, User: {} - Not found or access denied", id, userId);
            throw new AddressException(AddressError.UPDATE_FAILED);
        }

        log.info("Successfully updated address - AddressId: {}, User: {}", id, userId);
    }

    public void deleteAddress(Long id, String userId) {
        log.info("Deleting address - AddressId: {}, User: {}", id, userId);

        boolean deleted = addressRepository.deleteByIdAndUserId(id, userId);
        if (!deleted) {
            log.warn("Failed to delete address - AddressId: {}, User: {} - Not found or access denied", id, userId);
            throw new AddressException(AddressError.DELETE_FAILED);
        }

        log.info("Successfully deleted address - AddressId: {}, User: {}", id, userId);
    }
}
