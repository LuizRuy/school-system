package com.school.school.model.dto.attendance;

import lombok.Data;

import java.util.HashMap;
import java.util.Set;

@Data
public class AttendanceRequest {
    private Long classSessionId;
    private HashMap<Long, Boolean> studentsPresence;

}
