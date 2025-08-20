package com.example.backend.dto.Game.piece;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.PositionDto;
import com.example.backend.enums.Color;
import com.example.backend.enums.MoveFlag;
import com.example.backend.enums.PieceType;

import java.util.ArrayList;
import java.util.Collection;

public class Pawn extends Piece {

    private boolean hasMoved;

    public Pawn(PositionDto position, Color color, PieceType type) {
        this.position = position;
        this.color = color;
        this.type = type;
        this.hasMoved = false;
    }

    @Override
    public Collection<MoveDto> getMoves(Chessboard board) {
        int dir;
        Collection<MoveDto> moves = new ArrayList<>();

        if (this.color == Color.BLACK) {
            dir = -1;
        } else {
            dir = 1;
        }

        moves.addAll(board.pieces().stream().filter(piece -> piece.getColor() != this.color)
                .filter(piece -> piece.position.x() == this.position.x() + dir + 1 || piece.position.x() == this.position.x() + dir - 1)
                .map(piece -> new MoveDto(this.position, piece.position, this, MoveFlag.CAPTURE, null)).toList());

        moves.addAll(board.pieces().stream()
                .filter(piece -> piece.getPosition()
                        .equals(new PositionDto(this.position.x(), this.position.y() + dir)))
                .map(piece -> new MoveDto(this.position, piece.position, this, MoveFlag.NORMAL, null))
                .toList());

        if (!this.hasMoved && board.pieces().stream().noneMatch(piece -> piece.getPosition()
                .equals(new PositionDto(this.position.x(), this.position.y() + dir)) || piece.getPosition()
                .equals(new PositionDto(this.position.x(), this.position.y() + dir * 2)))) {
            moves.add(new MoveDto(this.position, new PositionDto(this.position.x(), this.position.y() + dir * 2),
                    this, MoveFlag.NORMAL, null));
        }

        return moves;
    }
}
