package com.school.school.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data

public class ChangePasswordRequest {
    @NotBlank(message = "Senha antiga é obrigatorio")
    @Size(min = 8, max = 100, message = "Senha antiga deve ter entre 8 e 100 caracteres")
    private String oldPassword;

    @NotBlank(message = "Senha nova é obrigatorio")
    @Size(min = 8, max = 100, message = "Senha nova deve ter entre 8 e 100 caracteres")
    private String newPassword;

    @NotBlank(message = "Confirmação da nova senha é obrigatorio")
    @Size(min = 8, max = 100, message = "Confirmação da senha deve ter entre 8 e 100 caracteres")
    private String confirmNewPassword;
}
