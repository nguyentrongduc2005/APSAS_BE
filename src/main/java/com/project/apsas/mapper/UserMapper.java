package com.project.apsas.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    LoginResponse toLoginResponse(User user);
}
