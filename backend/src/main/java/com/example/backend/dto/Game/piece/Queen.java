package com.example.backend.dto.Game.piece;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.PositionDto;
import com.example.backend.enums.Color;
import com.example.backend.enums.PieceType;

import java.util.Collection;
import java.util.List;

public class Queen extends Piece{

    public Queen(PositionDto position, Color color, PieceType type, boolean hasMoved) {
        super(position, color, type, hasMoved);
    }

    @Override
    public Collection<MoveDto> getMoves(Chessboard board, boolean validateForCheck) {
        return checkMoves(this.rekursiveStep(this.position, board,
                List.of(1, 1, -1, -1, 1, -1, 0, 0),
                List.of(1, -1, -1, 1, 0, 0, -1, 1)), board, validateForCheck);
    }

    @Override
    public Piece clone() {
        return new Queen(position, color, type, hasMoved);
    }
}
