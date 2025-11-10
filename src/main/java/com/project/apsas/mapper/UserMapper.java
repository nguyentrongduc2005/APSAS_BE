package com.project.apsas.mapper;

import com.project.apsas.dto.response.AuthUserDto;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles",
             expression = "java(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))")
    // Nếu entity có field avatar tên khác, thêm: @Mapping(target="avatar", source="avatarFieldName")
    AuthUserDto toAuthUserDto(User user);
}
