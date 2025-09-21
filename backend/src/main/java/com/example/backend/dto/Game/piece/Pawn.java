package com.example.backend.dto.Game.piece;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.PositionDto;
import com.example.backend.enums.Color;
import com.example.backend.enums.MoveFlag;
import com.example.backend.enums.PieceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Pawn extends Piece {

    private boolean isEnPassant = false;

    public Pawn(PositionDto position, Color color, PieceType type, boolean hasMoved) {
        super(position, color, type, hasMoved);
    }

    @Override
    public Collection<MoveDto> getMoves(Chessboard board, boolean validateForCheck) {
        int dir;
        List<MoveDto> moves = new ArrayList<>();

        if (this.color == Color.BLACK) {
            dir = -1;
        } else {
            dir = 1;
        }

        for (Piece piece : board.pieces()) {
            if ((new PositionDto(this.position.x() + 1, this.position.y() + dir).equals(piece.getPosition()) ||
                    new PositionDto(this.position.x() - 1, this.position.y() + dir).equals(piece.getPosition())) && piece.getColor() != this.color) {
                moves.add(new MoveDto(this.position, piece.position, this, MoveFlag.CAPTURE, null));
            }
        }

        if (board.pieces().stream()
                .noneMatch(piece -> piece.getPosition()
                        .equals(new PositionDto(this.position.x(), this.position.y() + dir)))) {
            moves.add(new MoveDto(this.position, new PositionDto(this.position.x(), this.position.y() + dir), this, MoveFlag.NORMAL, null));
        }

        if (!this.hasMoved && board.pieces().stream().noneMatch(piece -> piece.getPosition()
                .equals(new PositionDto(this.position.x(), this.position.y() + dir)) || piece.getPosition()
                .equals(new PositionDto(this.position.x(), this.position.y() + dir * 2)))) {
            moves.add(new MoveDto(this.position, new PositionDto(this.position.x(), this.position.y() + dir * 2),
                    this, MoveFlag.NORMAL, null));
        }

        return checkMoves(moves, board, validateForCheck);
    }

    @Override
    public void massageMove() {
        isEnPassant = !this.hasMoved;
        super.massageMove();
    }

    @Override
    public Piece clone() {
        return new Pawn(position, color, type, hasMoved);
    }
}
