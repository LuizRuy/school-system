package com.school.school.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserResponse {

    private String firstName;
    private String lastName;
    private String email;
}
