package com.bookstore.backend.mappers;

import com.bookstore.backend.dtos.UserAccount;
import com.bookstore.backend.entities.UserAccountEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserAccountMapper {

    UserAccountMapper INSTANCE = Mappers.getMapper(UserAccountMapper.class);

    UserAccount toDto(UserAccountEntity entity);

    UserAccountEntity toEntity(UserAccount dto);
}
