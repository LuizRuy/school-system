package com.school.school.mapper;

import com.school.school.model.Student;
import com.school.school.model.User;
import com.school.school.model.dto.student.CreateStudentRequest;
import com.school.school.model.dto.student.StudentResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class StudentMapper {

    public Student toEntity(CreateStudentRequest dto, User user) {
        return Student.builder()
                .name(dto.getName())
                .dateOfBirth(dto.getDateOfBirth())
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public StudentResponse toDto(Student student) {
        return StudentResponse.builder()
                .id(student.getId())
                .name(student.getName())
                .birthDate(student.getDateOfBirth())
                .observation(student.getObservations())
                .build();
    }
}
