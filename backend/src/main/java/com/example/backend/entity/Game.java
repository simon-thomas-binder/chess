package com.example.backend.entity;

import com.example.backend.enums.GameStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "game")
public class Game {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private GameStatus status;

    @Column(name = "board_width", nullable = false)
    private int boardWidth;

    @Column(name = "board_height", nullable = false)
    private int boardHeight;

    @Column(name = "initial_time_ms", nullable = false)
    private int initialTime;

    @Column(name = "increment_ms", nullable = false)
    private int increment;

    @Column(name = "delay_ms", nullable = false)
    private int delay;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
