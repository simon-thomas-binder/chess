package com.example.backend.dto.Game;

import com.example.backend.dto.Game.piece.Piece;
import com.example.backend.enums.Color;
import com.example.backend.enums.MoveFlag;
import com.example.backend.enums.PieceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.ArrayList;
import java.util.List;

/**
 * Representation of a chessboard
 *
 * @param width
 * @param height
 * @param initial_time
 * @param increment
 * @param delay
 * @param pieces List of pieces on the board
 */
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

    /**
     * Gets piece at given position or null
     *
     * @param position to search for
     * @return Piece or null
     */
    public Piece getPiece(PositionDto position) {
        return pieces.stream().filter(piece -> piece.getPosition().equals(position)).findFirst().orElse(null);
    }

    /**
     * Checks if given color is in check (e.g. their king can be taken)
     *
     * @param color of the king
     * @return true if in check
     */
    public boolean isInCheck(Color color) {
        Piece king = pieces.stream()
                .filter(piece -> piece.getColor().equals(color) && piece.getType() == PieceType.KING)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No king piece found for color: " + color));

        return isUnderAttack(king.getPosition(), Color.getOtherColor(color));
    }

    /**
     * Calculates if given position is under attack by a piece from given player
     *
     * @param position to check
     * @param color of attacking player
     * @return true/false
     */
    public boolean isUnderAttack(PositionDto position, Color color) {
        for (Piece piece : pieces) {
            if (piece.getColor().equals(color) && piece.getMoves(this, false).stream().anyMatch(move -> move.to().equals(position))) {
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
