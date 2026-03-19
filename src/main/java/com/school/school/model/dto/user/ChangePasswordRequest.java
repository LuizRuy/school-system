package com.school.school.model.dto.user;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data

public class ChangePasswordRequest {
    @NotBlank(message = "Senha antiga é obrigatorio")
    private String oldPassword;

    @NotBlank(message = "Senha nova é obrigatorio")
    private String newPassword;

    @NotBlank(message = "Confirmação da nova senha é obrigatorio")
    private String confirmNewPassword;
}
