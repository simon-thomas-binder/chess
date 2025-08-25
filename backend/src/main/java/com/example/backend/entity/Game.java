package com.example.backend.entity;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.enums.Color;
import com.example.backend.enums.GameEndFlag;
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

    @Column(name = "winner", nullable = true)
    @Enumerated(EnumType.STRING)
    private Color winner;

    @Column(name = "end_game_flag", nullable = true)
    @Enumerated(EnumType.STRING)
    private GameEndFlag endReason;

    @Column(name = "end_time", nullable = true, updatable = false)
    private Instant endTime;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Sets up a new Game Entity
     *
     * @param board info
     * @return new game object
     */
    public static Game newGame(Chessboard board) {
        Game game = new Game();
        game.setBoardHeight(board.height());
        game.setBoardWidth(board.width());
        game.setIncrement(board.increment());
        game.setDelay(board.delay());
        game.setInitialTime(board.initial_time());
        game.setStatus(GameStatus.PENDING);
        return game;
    }

    /**
     * Setup for ending this game
     *
     * @param game to end
     * @param winner to add (can be null for remi)
     * @return game object
     */
    public static Game endGame(Game game, GameEndFlag flag, Color winner) {
        game.setStatus(GameStatus.FINISHED);
        game.setEndReason(flag);
        game.setWinner(winner);
        game.setEndTime(Instant.now());
        return game;
    }
}
