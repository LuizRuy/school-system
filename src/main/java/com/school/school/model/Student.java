package com.school.school.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDate dateOfBirth;

    private String observations;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany
    @JoinTable(
            name= "student_classroom",
            joinColumns = @JoinColumn(name = "student_id"),
            inverseJoinColumns = @JoinColumn(name = "classroom_id")
    )
    private Set<Classroom> classrooms = new HashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<Submission> submissions = new HashSet<>();

    @OneToMany(mappedBy = "student")
    private Set<Attendance> attendances = new HashSet<>();

    public void addClassroom(Classroom classroom) {
        classrooms.add(classroom);
        classroom.getStudents().add(this);
    }
}
