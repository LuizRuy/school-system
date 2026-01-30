package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.model.ClassSession;
import com.school.school.model.User;
import com.school.school.model.dto.classsession.ClassSessionResponse;
import com.school.school.repository.ClassSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassSessionService {

    private final ClassSessionRepository classSessionRepository;

    public void createClassSession(UserAuthenticated userAuthenticated) {
        User user = userAuthenticated.getUser();
        ClassSession classSession = new ClassSession();
        classSession.setCreatedAt(LocalDateTime.now());
        classSession.setUser(user);

        classSessionRepository.save(classSession);
    }

    public ClassSession getClassSessionById(Long id, UserAuthenticated userAuthenticated) {
        ClassSession classSession =  classSessionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Class session not found with id: " + id));

        if(!classSession.getUser().getId().equals(userAuthenticated.getUser().getId())) {
            throw new EntityNotFoundException("Class session not found with id: " + id);
        }

        return classSession;
    }

    public ClassSessionResponse getById(Long id, UserAuthenticated userAuthenticated) {
        ClassSession classSession = getClassSessionById(id, userAuthenticated);

        ClassSessionResponse classSessionResponse = new ClassSessionResponse();
        classSessionResponse.setId(classSession.getId());
        classSessionResponse.setCreatedAt(classSession.getCreatedAt());

        return classSessionResponse;
    }

    public List<ClassSessionResponse> getUserClassSessions(UserAuthenticated userAuthenticated) {
        User user = userAuthenticated.getUser();

        List<ClassSession> classSessions = classSessionRepository.findByUserId(user.getId());

        return classSessions.stream().map(classSession -> {
            ClassSessionResponse classSessionResponse = new ClassSessionResponse();
            classSessionResponse.setId(classSession.getId());
            classSessionResponse.setCreatedAt(classSession.getCreatedAt());
            return classSessionResponse;
        }).toList();
    }

    public void deleteClassSession(Long id, UserAuthenticated userAuthenticated) {
        ClassSession classSession = getClassSessionById(id, userAuthenticated);
        classSessionRepository.delete(classSession);
    }
}
