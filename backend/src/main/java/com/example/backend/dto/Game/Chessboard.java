package com.example.backend.dto.Game;

import com.example.backend.dto.Game.piece.Piece;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.List;

public record Chessboard(@Min(3) @Max(64) int width, @Min(3) @Max(64) int height, @Min(1) int initial_time, int increment, int delay, List<Piece> pieces) {

    /**
     * Moves a piece of this board
     *
     * @param moveDto move data
     */
    public void move(MoveDto moveDto) {
        for (Piece piece : pieces) {
            if (piece.getPosition().equals(moveDto.from())) {
                piece.setPosition(moveDto.to());
                piece.massageMove();
            }
        }
    }
}
