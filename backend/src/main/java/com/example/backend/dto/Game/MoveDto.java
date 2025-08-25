package com.example.backend.dto.Game;

import com.example.backend.dto.Game.piece.Piece;
import com.example.backend.enums.MoveFlag;
import com.example.backend.enums.PieceType;

public record MoveDto(PositionDto from, PositionDto to, Piece piece, MoveFlag flag, PieceType promotionTo) {
}
