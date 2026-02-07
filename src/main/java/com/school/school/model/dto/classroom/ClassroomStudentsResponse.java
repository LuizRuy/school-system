package com.school.school.model.dto.classroom;

import com.school.school.model.Student;
import com.school.school.model.dto.student.StudentClassroomResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClassroomStudentsResponse {
    private Long id;
    private String name;
    private List<StudentClassroomResponse> students;
}
