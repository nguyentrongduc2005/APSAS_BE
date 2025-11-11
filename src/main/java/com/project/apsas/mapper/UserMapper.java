package com.project.apsas.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.project.apsas.dto.response.AuthUserDto;
import com.project.apsas.dto.response.LoginResponse;
import com.project.apsas.entity.Role;
import com.project.apsas.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mappings({
            @Mapping(target = "accessToken", ignore = true),   // service sẽ set
            @Mapping(target = "refreshToken", ignore = true),  // service sẽ set
            @Mapping(target = "user", expression = "java(toAuthUserDto(user))")
    })
    LoginResponse toLoginResponse(User user);

    @Mappings({
            @Mapping(target = "roles", expression = "java(mapRoles(user.getRoles()))"),
            // nếu AuthUserDto không có avatar, để ignore = true
            @Mapping(target = "avatar", ignore = true)
    })
    AuthUserDto toAuthUserDto(User user);

    default java.util.Set<String> mapRoles(java.util.Set<Role> roles) {
        if (roles == null) return java.util.Collections.emptySet();
        return roles.stream()
                .map(r -> r.getName() != null ? r.getName() : String.valueOf(r.getId()))
                .collect(java.util.stream.Collectors.toSet());
    }
}
