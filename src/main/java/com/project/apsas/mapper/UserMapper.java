package com.project.apsas.mapper;

import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    LoginResponse toLoginResponse(User user);

}
