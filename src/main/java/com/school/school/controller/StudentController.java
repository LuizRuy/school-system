package com.school.school.controller;

import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.dto.student.CreateStudentRequest;
import com.school.school.model.dto.student.StudentResponse;
import com.school.school.model.dto.student.UpdateStudent;
import com.school.school.service.StudentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/students")
public class StudentController {

    private final StudentService studentService;

    @PostMapping
    ResponseEntity<Void> createStudent(@RequestBody @Valid CreateStudentRequest request,
                                       @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        studentService.createStudent(request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{studentId}")
    ResponseEntity<UpdateStudent> updateStudent(@PathVariable Long studentId,
                                                @RequestBody @Valid UpdateStudent updateStudent,
                                                @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        UpdateStudent updatedStudent = studentService.updateStudent(studentId, updateStudent, authenticatedUser);
        return ResponseEntity.status(HttpStatus.OK).body(updatedStudent);
    }

    @DeleteMapping("/{studentId}")
    ResponseEntity<Void> deleteStudent(@PathVariable Long studentId,
                                       @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        studentService.deleteStudent(studentId, authenticatedUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{studentId}")
    ResponseEntity<StudentResponse> findById(@PathVariable Long studentId,
                                             @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        StudentResponse studentResponse = studentService.findByStudentId(studentId, authenticatedUser);
        return ResponseEntity.status(HttpStatus.OK).body(studentResponse);
    }

    @GetMapping("/user-students")
    ResponseEntity<List<StudentResponse>> findStudentsByUser(@AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        List<StudentResponse> students = studentService.findStudentsByUser(authenticatedUser);
        return ResponseEntity.status(HttpStatus.OK).body(students);
    }
}
