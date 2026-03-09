package com.example.backend.enums;

import com.example.backend.dto.Game.PositionDto;
import com.example.backend.dto.Game.piece.Bishop;
import com.example.backend.dto.Game.piece.King;
import com.example.backend.dto.Game.piece.Knight;
import com.example.backend.dto.Game.piece.Pawn;
import com.example.backend.dto.Game.piece.Piece;
import com.example.backend.dto.Game.piece.Queen;
import com.example.backend.dto.Game.piece.Rook;

public enum PieceType {
    KING,
    QUEEN,
    KNIGHT,
    BISHOP,
    ROOK,
    PAWN;

    public static Piece createPiece(PositionDto position, Color color, PieceType type) {

        switch (type) {
            case KING -> {return new King(position, color, type, true);}
            case QUEEN -> {return new Queen(position, color, type, true);}
            case KNIGHT -> {return new Knight(position, color, type, true);}
            case BISHOP -> {return new Bishop(position, color, type, true);}
            case ROOK -> {return new Rook(position, color, type, true);}
            case PAWN -> {return new Pawn(position, color, type, true);}
        }

        throw new IllegalArgumentException("No valid PieceType found for " + type);
    }

}