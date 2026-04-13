package com.school.school.service;

import com.school.school.infra.exception.EntityNotFoundException;
import com.school.school.infra.security.UserAuthenticated;
import com.school.school.mapper.ClassSessionMapper;
import com.school.school.model.ClassSession;
import com.school.school.model.User;
import com.school.school.model.dto.classsession.ClassSessionResponse;
import com.school.school.repository.ClassSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClassSessionService {

    private final ClassSessionRepository classSessionRepository;
    private final ClassSessionMapper classSessionMapper;

    public void createClassSession(UserAuthenticated userAuthenticated) {
        User user = userAuthenticated.getUser();

        classSessionRepository.save(classSessionMapper.toEntity(user));
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

        return classSessionMapper.toDto(classSession);
    }

    public List<ClassSessionResponse> getUserClassSessions(UserAuthenticated userAuthenticated) {
        User user = userAuthenticated.getUser();

        List<ClassSession> classSessions = classSessionRepository.findByUserId(user.getId());

        return classSessions.stream()
                .map(classSessionMapper::toDto)
                .toList();
    }

    public void deleteClassSession(Long id, UserAuthenticated userAuthenticated) {
        ClassSession classSession = getClassSessionById(id, userAuthenticated);
        classSessionRepository.delete(classSession);
    }
}
