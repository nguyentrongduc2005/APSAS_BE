package com.project.apsas.entity;

import com.project.apsas.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.AUTO)
    @JdbcTypeCode(SqlTypes.CHAR)
    UUID id;
    String name;
    String email;
    String password;
    @Enumerated(EnumType.STRING)
    UserStatus status;

}
