package com.school.school.controller;

import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.dto.attendance.AttendanceRequest;
import com.school.school.service.AttendanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/attendances")
public class AttendanceController {

    private final AttendanceService attendanceService;

    @PostMapping()
    public ResponseEntity<Void> createAttendanceList(@RequestBody AttendanceRequest attendanceRequest,
                                                     @AuthenticationPrincipal UserAuthenticated userAuthenticated){
        attendanceService.markAttendanceList(attendanceRequest,userAuthenticated);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/{classSessionId}/{studentId}/{presence}")
    public ResponseEntity<Void> createAttendance(@PathVariable Long classSessionId,
                                                 @PathVariable Long studentId,
                                                 @PathVariable boolean presence,
                                                 @AuthenticationPrincipal UserAuthenticated userAuthenticated){
        attendanceService.markAttendance(classSessionId,studentId,presence,userAuthenticated);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

}
