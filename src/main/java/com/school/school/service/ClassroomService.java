package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.Classroom;
import com.school.school.model.Student;
import com.school.school.model.User;
import com.school.school.model.dto.classroom.AddStudentsRequest;
import com.school.school.model.dto.classroom.ClassroomRequest;
import com.school.school.model.dto.classroom.ClassroomResponse;
import com.school.school.repository.ClassroomRepository;
import com.school.school.repository.StudentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final StudentService studentService;
    private final StudentRepository studentRepository;

    public void createClassroom(ClassroomRequest createClassroomRequest, UserAuthenticated userAuthenticated) {

        User user = userAuthenticated.getUser();

        Classroom classroom = new Classroom();
        classroom.setName(createClassroomRequest.getName());
        classroom.setUser(user);
        classroom.setCreatedAt(LocalDateTime.now());

        classroomRepository.save(classroom);

    }

    public Classroom findById(Long classroomId, UserAuthenticated userAuthenticated) {
        Classroom c =  classroomRepository.findById(classroomId)
                .orElseThrow(() -> new EntityNotFoundException("Classroom not found with id: " + classroomId));

        if(!c.getUser().getId().equals(userAuthenticated.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to access this classroom");
        }

        return c;
    }

    public ClassroomResponse findClassroomById(Long classroomId, UserAuthenticated userAuthenticated) {
        Classroom classroom = findById(classroomId, userAuthenticated);

        return new ClassroomResponse(
                classroom.getId(),
                classroom.getName()
        );
    }

    public List<ClassroomResponse> findAllClassrooms(UserAuthenticated userAuthenticated) {
        User user = userAuthenticated.getUser();
        List<Classroom> classrooms = classroomRepository.findByUserId(user.getId());

        return classrooms.stream()
                .map(c -> new ClassroomResponse(c.getId(), c.getName()))
                .toList();
    }

    public void deleteClassroomById(Long classroomId, UserAuthenticated userAuthenticated) {
        Classroom classroom = findById(classroomId, userAuthenticated);
        classroomRepository.delete(classroom);
    }

    public ClassroomResponse updateClassroom(Long classroomId, ClassroomRequest updateClassroomRequest, UserAuthenticated userAuthenticated) {
        Classroom classroom = findById(classroomId, userAuthenticated);

        classroom.setName(updateClassroomRequest.getName());
        classroom.setUpdatedAt(LocalDateTime.now());

        classroomRepository.save(classroom);

        return new ClassroomResponse(
                classroom.getId(),
                classroom.getName()
        );
    }

    public void assignClassroomToStudent(Long classroomId, Long studentId, UserAuthenticated userAuthenticated) {
        Classroom classroom = findById(classroomId, userAuthenticated);

        Student student = studentService.findStudent(studentId, userAuthenticated);

        student.addClassroom(classroom);

        studentRepository.save(student);
    }

    public void assignClassroomToStudents(Long classroomId, AddStudentsRequest request, UserAuthenticated userAuthenticated) {
        Classroom classroom = findById(classroomId, userAuthenticated);

        Set<Long> studentIds = request.getStudents();

        studentIds.forEach(s -> {
            Student student = studentService.findStudent(s, userAuthenticated);
            student.addClassroom(classroom);
        });

        studentRepository.saveAll(classroom.getStudents());
    }

    public void removeClassroomFromStudent(Long classroomId, Long studentId, UserAuthenticated userAuthenticated) {
        Classroom classroom = findById(classroomId, userAuthenticated);

        Student student = studentService.findStudent(studentId, userAuthenticated);

        student.getClassrooms().remove(classroom);

        studentRepository.save(student);
    }
}
