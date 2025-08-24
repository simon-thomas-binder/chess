package com.example.backend.dto.Game.piece;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import lombok.AllArgsConstructor;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
public class King extends Piece {

    @Override
    public Collection<MoveDto> getMoves(Chessboard board) {
        return this.step(board, List.of(1, 1, 1, 0, 0, -1, -1, -1), List.of(1, 0, -1, 1, -1, 1, 0, -1));
    }
}
