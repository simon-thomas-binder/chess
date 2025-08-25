package com.example.backend.dto.Game;

import com.example.backend.dto.Game.piece.Piece;
import com.example.backend.enums.Color;
import com.example.backend.enums.MoveFlag;
import com.example.backend.enums.PieceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.ArrayList;
import java.util.List;

public record Chessboard(@Min(3) @Max(64) int width, @Min(3) @Max(64) int height, @Min(1) int initial_time, int increment, int delay, List<Piece> pieces) {

    /**
     * Moves a piece of this board
     *
     * @param moveDto move data
     */
    public void move(MoveDto moveDto) {
        if (moveDto.flag() == MoveFlag.CAPTURE) {
            pieces.removeIf(piece -> piece.getPosition().equals(moveDto.to()));
        }
        for (Piece piece : pieces) {
            if (piece.getPosition().equals(moveDto.from())) {
                piece.setPosition(moveDto.to());
                piece.massageMove();
            }
        }
    }

    public boolean isInCheck(Color color) {
        Piece king = null;
        for (Piece piece : pieces) {
            if (piece.getColor().equals(color) && piece.getType() == PieceType.KING) {
                king = piece;
            }
        }
        if (king == null) {
            throw new IllegalStateException("No king piece found");
        }

        for (Piece piece : pieces) {
            Piece finalKing = king;
            if (piece.getMoves(this, false).stream().anyMatch(move -> move.to().equals(finalKing.getPosition()))) {
                return true;
            }
        }
        return false;
    }

    public List<MoveDto> getAllMoves(Color color) {
        List<MoveDto> moves = new ArrayList<>();
        for (Piece piece : pieces) {
            if (piece.getColor().equals(color)) {
                moves.addAll(piece.getMoves(this, true));
            }
        }
        return moves;
    }

    public Chessboard clone() {
        List<Piece> clonedPieces = new ArrayList<>();
        for (Piece piece : pieces) {
            clonedPieces.add(piece.clone());
        }
        return new Chessboard(width, height, initial_time, increment, delay, clonedPieces);
    }
}
