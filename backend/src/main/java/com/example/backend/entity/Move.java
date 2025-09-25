package com.example.backend.entity;

import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.Player;
import com.example.backend.enums.Color;
import com.example.backend.enums.MoveFlag;
import com.example.backend.enums.PieceType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "move",
        uniqueConstraints = @UniqueConstraint(columnNames = {"game_id", "ply"}))
@Getter
@Setter
public class Move {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private int ply;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Color actor;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "from_x")),
            @AttributeOverride(name = "y", column = @Column(name = "from_y"))
    })
    private Position from;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "x", column = @Column(name = "to_x")),
            @AttributeOverride(name = "y", column = @Column(name = "to_y"))
    })
    private Position to;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PieceType piece;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MoveFlag move = MoveFlag.NORMAL;

    @Enumerated(EnumType.STRING)
    private PieceType promotionTo;

    private Long actorTimeLeftMsAfter;

    @CreationTimestamp
    @Column(name = "server_received_at", nullable = false, updatable = false)
    private Instant serverReceivedAt;

    /**
     * Creates a new Move entity
     *
     * @param move dto with move infos
     * @param game game entity
     * @param player who plays this move
     * @param ply number of this move
     */
    public Move(MoveDto move, Game game, Player player, long timeLeft, int ply) {
        this.setGame(game);
        this.setFrom(new Position(move.from().x(), move.from().y()));
        this.setTo(new Position(move.to().x(), move.to().y()));
        this.setMove(move.flag());
        this.setActor(player.color());
        this.setPiece(move.piece().getType());
        this.setPly(ply);
        this.setPromotionTo(move.promotionTo());
        this.setServerReceivedAt(Instant.now());
        this.setActorTimeLeftMsAfter(timeLeft);
    }

    public Move() {

    }
}
