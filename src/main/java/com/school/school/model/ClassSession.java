package com.school.school.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
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
@EntityListeners(AuditingEntityListener.class)
public class ClassSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //todo: trocar para LocalDate
    @CreatedDate
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "classSession")
    private Set<Attendance> attendances = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name= "user_id")
    private  User user;
}
