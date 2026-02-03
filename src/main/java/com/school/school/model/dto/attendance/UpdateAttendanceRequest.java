package com.school.school.model.dto.attendance;

import lombok.Data;

@Data
public class UpdateAttendanceRequest {
    private Long classSessionId;
    private Long studentId;
    private boolean presence;
}
