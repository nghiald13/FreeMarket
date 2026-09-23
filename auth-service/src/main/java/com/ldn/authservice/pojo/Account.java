package com.ldn.authservice.pojo;

import com.ldn.authservice.enums.AccountStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "email", nullable = false, unique = true)
    String email;

    @Column(name = "password", nullable = false)
    String password;

    @Column(name = "name", length = 40, nullable = false)
    String name;

    @Column(name = "phone", length = 10, nullable = false, unique = true)
    String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    AccountStatus status;

    @Column(name = "is_mfa_enabled")
    boolean isMfaEnabled;

    @Column(name = "mfa_secret", nullable = false, unique = true)
    String mfaSecret;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
