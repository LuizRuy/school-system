package com.school.school.mapper;

import com.school.school.model.User;
import com.school.school.model.dto.user.UserDTO;
import com.school.school.model.dto.user.UserRequest;
import com.school.school.model.enums.Role;
import com.school.school.model.enums.Status;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public User toEntity(UserRequest dto, String hashedPassword) {
        return User.builder()
                .email(dto.getEmail())
                .password(hashedPassword)
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .createdAt(LocalDateTime.now())
                .role(Role.USER)
                .status(Status.ENABLED)
                .build();
    }

    public UserDTO toDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .status(user.getStatus().toString())
                .build();
    }
}
