package com.school.school.model.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {

    @NotBlank(message = "Nome é obrigatorio")
    @Size(max = 80, message = "Nome deve ter no máximo 80 caracteres")
    private String firstName;

    @NotBlank(message = "Sobrenome é obrigatorio")
    @Size(max = 80, message = "Sobrenome deve ter no máximo 80 caracteres")
    private String lastName;

    @NotBlank(message = "Email é obrigatorio")
    @Email(message = "Email inválido")
    @Size(max = 120, message = "Email deve ter no máximo 120 caracteres")
    private String email;

    @NotBlank(message = "Senha é obrigatorio")
    @Size(min = 8, max = 100, message = "Senha deve ter entre 8 e 100 caracteres")
    private String password;
}
