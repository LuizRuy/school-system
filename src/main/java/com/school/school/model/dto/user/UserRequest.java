package com.school.school.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Nome é obrigatorio")
    private String firstName;

    @NotBlank(message = "Sobrenome é obrigatorio")
    private String lastName;

    @NotBlank(message = "Email é obrigatorio")
    private String email;

    @NotBlank(message = "Senha é obrigatorio")
    private String password;
}
