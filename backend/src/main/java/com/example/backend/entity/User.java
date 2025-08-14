package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * User entity
 */
@Entity
@Getter
@Setter
@Table(name = "app_user")
public class User {

    @Id
    private String username;

    @Column(nullable = false)
    private String displayname;

    @Column(name = "password_hash", nullable = false)
    private String password;

    @Column(nullable = false)
    private String salt;

    @Column(nullable = false)
    private Integer rating = 1200;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
