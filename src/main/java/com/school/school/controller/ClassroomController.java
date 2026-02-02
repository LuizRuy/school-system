package com.school.school.controller;

import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.dto.classroom.AddStudentsRequest;
import com.school.school.model.dto.classroom.ClassroomRequest;
import com.school.school.model.dto.classroom.ClassroomResponse;
import com.school.school.service.ClassroomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/classrooms")
public class ClassroomController {

    private final ClassroomService classroomService;

    @PostMapping
    public ResponseEntity<Void> createClassroom(@RequestBody ClassroomRequest createClassroomRequest,
                                                @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        classroomService.createClassroom(createClassroomRequest, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{classroomId}")
    public ResponseEntity<ClassroomResponse> getClassroom(@PathVariable Long classroomId,
                                                          @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        ClassroomResponse c = classroomService.findClassroomById(classroomId, authenticatedUser);
        return ResponseEntity.ok(c);
    }

    @GetMapping("/user-classrooms")
    public ResponseEntity<List<ClassroomResponse>> getAllClassrooms(@AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        List<ClassroomResponse> classrooms = classroomService.findAllClassrooms(authenticatedUser);
        return ResponseEntity.ok(classrooms);
    }

    @DeleteMapping("/{classroomId}")
    public ResponseEntity<Void> deleteById(@PathVariable Long classroomId,
                                           @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        classroomService.deleteClassroomById(classroomId, authenticatedUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/{classroomId}")
    public ResponseEntity<ClassroomResponse> updateClassroom(@PathVariable Long classroomId,
                                                                @RequestBody ClassroomRequest classroomRequest,
                                                                @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        ClassroomResponse updatedClassroom = classroomService.updateClassroom(classroomId, classroomRequest, authenticatedUser);
        return ResponseEntity.ok(updatedClassroom);
    }

    //todo: testar endpoints


    @PostMapping("{classroomId}/students/{studentId}")
    public ResponseEntity<Void> assignNewStudent(@PathVariable Long classroomId,
                                                 @PathVariable Long studentId,
                                                 @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        classroomService.assignClassroomToStudent(classroomId, studentId, authenticatedUser);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("{classroomId}/students")
    public ResponseEntity<Void> assignNewStudents(@PathVariable Long classroomId,
                                                  @RequestBody AddStudentsRequest request,
                                                  @AuthenticationPrincipal UserAuthenticated authenticatedUser){
        classroomService.assignClassroomToStudents(classroomId, request,authenticatedUser);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("{classroomId}/student/{studentId}")
    public ResponseEntity<Void> removeClassroomFromStudent(@PathVariable Long classroomId,
                                                           @PathVariable Long studentId,
                                                           @AuthenticationPrincipal UserAuthenticated authenticatedUser) {
        classroomService.removeClassroomFromStudent(classroomId, studentId, authenticatedUser);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
