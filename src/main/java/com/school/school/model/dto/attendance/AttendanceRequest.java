package com.school.school.model.dto.attendance;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.HashMap;
import java.util.Set;

@Data
public class AttendanceRequest {
    @NotNull(message = "O id da sessão de aula é obrigatório.")
    private Long classSessionId;

    @NotEmpty
    private HashMap<Long, Boolean> studentsPresence;

}
