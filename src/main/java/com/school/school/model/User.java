package com.school.school.model;

import com.school.school.model.enums.Role;
import com.school.school.model.enums.Status;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String firstName;

    private String lastName;

    private String email;

    private String password;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private Status status;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user")
    private Set<Student> students = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Classroom> classes = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<ClassSession> classSessions = new HashSet<>();
}
