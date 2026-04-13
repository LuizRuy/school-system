package com.school.school.mapper;

import com.school.school.model.ClassSession;
import com.school.school.model.User;
import com.school.school.model.dto.classsession.ClassSessionResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class ClassSessionMapper {

    public ClassSession toEntity(User user) {
        return ClassSession.builder()
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public ClassSessionResponse toDto(ClassSession classSession) {
        return ClassSessionResponse.builder()
                .id(classSession.getId())
                .createdAt(classSession.getCreatedAt())
                .build();
    }
}
