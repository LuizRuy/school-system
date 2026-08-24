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
import com.school.school.service.ownership.OwnershipGuard;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;
    private final OwnershipGuard<Student> studentOwnershipGuard;

    public void createStudent(CreateStudentRequest request, UserAuthenticated authenticatedUser) {

        User user = getAuthenticatedUser(authenticatedUser);

        studentRepository.save(studentMapper.toEntity(request, user));
    }

    public Student findStudent(Long id, UserAuthenticated authenticatedUser) {
        return studentOwnershipGuard.resolve(id, authenticatedUser);
    }

    public Map<Long, Student> resolveAll(Collection<Long> studentIds, UserAuthenticated authenticatedUser) {
        if (studentIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Student> resolved = new LinkedHashMap<>();
        studentRepository.findAllById(studentIds)
                .forEach(student -> resolved.put(student.getId(), student));

        Long ownerId = authenticatedUser.getUser().getId();
        for (Long studentId : studentIds) {
            Student student = resolved.get(studentId);
            if (student == null) {
                throw new EntityNotFoundException("Student not found with id: " + studentId);
            }
            if (!ownerId.equals(student.getUser().getId())) {
                throw new AccessDeniedException("You do not have permission to access this student");
            }
        }

        return resolved;
    }

    public UpdateStudent updateStudent(Long studentId, UpdateStudent updateStudent, UserAuthenticated authenticatedUser) {
        Student student = findStudent(studentId, authenticatedUser);

        student.setName(updateStudent.getName());
        student.setDateOfBirth(updateStudent.getDateOfBirth());
        student.setObservations(updateStudent.getObservations());

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
