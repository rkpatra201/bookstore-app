package com.bookstore.backend.services;

import com.bookstore.backend.dtos.CustomerAddress;
import com.bookstore.backend.entities.CustomerAddressEntity;
import com.bookstore.backend.mappers.AddressMapper;
import com.bookstore.backend.repositories.AddressRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper = AddressMapper.INSTANCE;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public void addAddress(String userId, CustomerAddress customerAddress) {
        CustomerAddressEntity entity = addressMapper.toEntity(customerAddress);
        entity.setUserId(userId);
        addressRepository.add(entity);
    }

    public List<CustomerAddress> getAllAddresses(String userId) {
        List<CustomerAddressEntity> entities = addressRepository.findAllByUserId(userId);
        return addressMapper.toDtoList(entities);
    }

    public CustomerAddress getAddressById(Long id, String userId) {
        CustomerAddressEntity entity = addressRepository.findByIdAndUserId(id, userId);
        if (entity == null) {
            throw new IllegalArgumentException("Address not found or access denied");
        }
        return addressMapper.toDto(entity);
    }

    public void updateAddress(String userId, Long id, CustomerAddress customerAddress) {
        CustomerAddressEntity entity = addressMapper.toEntity(customerAddress);
        entity.setId(id);
        entity.setUserId(userId);
        
        boolean updated = addressRepository.updateByIdAndUserId(entity);
        if (!updated) {
            throw new IllegalArgumentException("Failed to update address. Address not found or access denied");
        }
    }

    public void deleteAddress(Long id, String userId) {
        boolean deleted = addressRepository.deleteByIdAndUserId(id, userId);
        if (!deleted) {
            throw new IllegalArgumentException("Failed to delete address. Address not found or access denied");
        }
    }
}
