package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.mapper.StudentMapper;
import com.school.school.model.Student;
import com.school.school.model.User;
import com.school.school.model.dto.student.CreateStudentRequest;
import com.school.school.model.dto.student.StudentResponse;
import com.school.school.model.dto.student.UpdateStudent;
import com.school.school.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public void createStudent(CreateStudentRequest request, UserAuthenticated authenticatedUser) {

        User user = getAuthenticatedUser(authenticatedUser);

        studentRepository.save(studentMapper.toEntity(request, user));
    }

    public Student findStudent(Long id, UserAuthenticated authenticatedUser) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Student with id " + id + " not found"));

        if(!student.getUser().getId().equals(authenticatedUser.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this student");
        }

        return student;
    }

    public UpdateStudent updateStudent(Long studentId, UpdateStudent updateStudent, UserAuthenticated authenticatedUser) {
        Student student = findStudent(studentId, authenticatedUser);

        student.setName(updateStudent.getName());
        student.setDateOfBirth(updateStudent.getDateOfBirth());
        student.setObservations(updateStudent.getObservations());
        student.setUpdatedAt(LocalDateTime.now());

        studentRepository.save(student);

        return updateStudent;
    }

    public void deleteStudent(Long studentId, UserAuthenticated authenticatedUser) {
        Student student = findStudent(studentId, authenticatedUser);

        studentRepository.delete(student);
    }

    public StudentResponse findByStudentId(Long studentId, UserAuthenticated authenticatedUser) {
        Student student = findStudent(studentId, authenticatedUser);

        return studentMapper.toDto(student);
    }

    public List<StudentResponse> findStudentsByUser(UserAuthenticated authenticatedUser) {
        User user = getAuthenticatedUser(authenticatedUser);
        List<Student> students = studentRepository.findByUser(user);
        return students.stream()
                .map(studentMapper::toDto)
                .toList();
    }

    private User getAuthenticatedUser(UserAuthenticated authenticatedUser) {
        return authenticatedUser.getUser();
    }

    public Student getReferenceById(Long studentId) {
        return studentRepository.getReferenceById(studentId);
    }

}
