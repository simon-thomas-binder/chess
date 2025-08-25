package com.example.backend.dto.Game.piece;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.PositionDto;
import com.example.backend.enums.Color;
import com.example.backend.enums.PieceType;

import java.util.Collection;
import java.util.List;

public class Knight extends Piece {

    public Knight(PositionDto position, Color color, PieceType type, boolean hasMoved) {
        super(position, color, type, hasMoved);
    }

    @Override
    public Collection<MoveDto> getMoves(Chessboard board, boolean validateForCheck) {
        return checkMoves(this.step(board, List.of(2, 2, -2, -2, 1, 1, -1, -1), List.of(1, -1, 1, -1, 2, -2, 2, -2)), board, validateForCheck);
    }

    @Override
    public Piece clone() {
        return new Knight(position, color, type, hasMoved);
    }
}
