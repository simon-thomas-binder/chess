package com.example.backend.dto.Game;

import com.example.backend.dto.Game.piece.Piece;
import com.example.backend.enums.MoveFlag;
import com.example.backend.enums.PieceType;

/**
 * Represents a Move in the game
 *
 * @param from current position of the piece
 * @param to target position of the piece
 * @param piece to move
 * @param flag spezial flag for Castling / Capture / En passat / etc
 * @param promotionTo type of piece to promotet to or null
 */
public record MoveDto(PositionDto from, PositionDto to, Piece piece, MoveFlag flag, PieceType promotionTo) {
}
