package com.school.school.model.dto.user;

import com.school.school.model.enums.Role;
import lombok.Data;

@Data
public class UserRequest {

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;
}
