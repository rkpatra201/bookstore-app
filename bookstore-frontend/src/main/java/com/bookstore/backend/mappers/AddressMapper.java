package com.bookstore.backend.mappers;

import com.bookstore.backend.dtos.CustomerAddress;
import com.bookstore.backend.entities.CustomerAddressEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface AddressMapper {
    AddressMapper INSTANCE = Mappers.getMapper(AddressMapper.class);

    CustomerAddress toDto(CustomerAddressEntity entity);

    CustomerAddressEntity toEntity(CustomerAddress dto);

    List<CustomerAddress> toDtoList(List<CustomerAddressEntity> entities);
}
