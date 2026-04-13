package com.school.school.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private Instant expiresAt;
}
