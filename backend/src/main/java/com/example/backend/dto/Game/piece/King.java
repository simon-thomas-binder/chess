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

public class King extends Piece {

    public King(PositionDto position, Color color, PieceType type, boolean hasMoved) {
        super(position, color, type, hasMoved);
    }

    @Override
    public Collection<MoveDto> getMoves(Chessboard board, boolean validateForCheck) {
        List<MoveDto> moves = new ArrayList<>(this.step(board, List.of(1, 1, 1, 0, 0, -1, -1, -1), List.of(1, 0, -1, 1, -1, 1, 0, -1)));
        if (validateForCheck) {
            Collection<MoveDto> castleMoves = checkCastle(board);
            if (castleMoves != null) {
                moves.addAll(castleMoves);
            }
        }
        return checkMoves(moves, board, validateForCheck);
    }

    private Collection<MoveDto> checkCastle(Chessboard board) {
        Collection<MoveDto> moves = new ArrayList<>();
        Color opponent = Color.getOtherColor(this.color);

        // Check if king has moved or is in check
        if (this.hasMoved || board.isInCheck(this.color)) {
            return null;
        }

        // Queen-side castle
        if (!board.getPiece(new PositionDto(0, this.getPosition().y())).hasMoved &&
                board.getPiece(new PositionDto(1, this.position.y())) == null &&
                board.getPiece(new PositionDto(2, this.position.y())) == null &&
                board.getPiece(new PositionDto(3, this.position.y())) == null &&
                !board.isUnderAttack(new PositionDto(2, this.position.y()), opponent) &&
                !board.isUnderAttack(new PositionDto(3, this.position.y()), opponent)
        ) {
            moves.add(new MoveDto(this.position, new PositionDto(this.position.x() - 2, this.position.y()), this, MoveFlag.CASTLE_QUEEN, null));
        }

        // King-side castle
        if (!board.getPiece(new PositionDto(board.width() - 1, this.getPosition().y())).hasMoved &&
                board.getPiece(new PositionDto(board.width() - 2, this.position.y())) == null &&
                board.getPiece(new PositionDto(board.width() - 3, this.position.y())) == null &&
                !board.isUnderAttack(new PositionDto(board.width() - 3, this.position.y()), opponent)
        ) {
            moves.add(new MoveDto(this.position, new PositionDto(this.position.x() + 2, this.position.y()), this, MoveFlag.CASTLE_KING, null));
        }

        return moves;
    }

    @Override
    public Piece clone() {
        return new King(position, color, type, hasMoved);
    }
}
