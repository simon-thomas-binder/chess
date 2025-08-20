package com.example.backend.dto.Game.piece;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.PositionDto;
import com.example.backend.enums.Color;
import com.example.backend.enums.MoveFlag;
import com.example.backend.enums.PieceType;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Getter @Setter
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Pawn.class,  name = "PAWN"),
        @JsonSubTypes.Type(value = King.class,  name = "KING"),
        @JsonSubTypes.Type(value = Queen.class, name = "QUEEN"),
        @JsonSubTypes.Type(value = Rook.class,  name = "ROOK"),
        @JsonSubTypes.Type(value = Bishop.class,name = "BISHOP"),
        @JsonSubTypes.Type(value = Knight.class,name = "KNIGHT")
})
public abstract class Piece {

    protected PositionDto position;
    protected Color color;
    protected PieceType type;

    /**
     * Calculates all valid moves for this piece
     *
     * @param board where to search for moves
     * @return a Collection of all valid moves
     */
    public abstract Collection<MoveDto> getMoves(Chessboard board);

    /**
     * Recursively calculates possible moves from the current position in multiple directions
     * Calls rekursiveStep on all direction Pairs
     * Precondition: xDir and yDir must be of the same length
     *
     * @param current The current position of the piece being evaluated
     * @param board The chessboard containing all pieces
     * @param xDir The list of horizontal directions to move
     * @param yDir The list of vertical directions to move
     * @return A list of valid moves from the current position
     */
    protected List<MoveDto> rekursiveStep(PositionDto current, Chessboard board, List<Integer> xDir, List<Integer> yDir) {

        if (xDir.size() != yDir.size()) {
            throw new IllegalArgumentException("xDir and yDir must be of the same length!");
        }

        List<MoveDto> result = new ArrayList<>();

        for (int i = 0; i < xDir.size(); i++) {
            int xDirection = xDir.get(i);
            int yDirection = yDir.get(i);

            result.addAll(rekursiveStep(current, board, xDirection, yDirection));
        }
        return result;
    }

    /**
     * Recursively calculates possible moves in a given direction
     *
     * @param current the current position of the piece
     * @param board The chessboard containing all pieces
     * @param xDir The horizontal direction to move
     * @param yDir The vertical direction to move
     * @return A list of valid moves from the current position
     */
    protected List<MoveDto> rekursiveStep(PositionDto current, Chessboard board, int xDir, int yDir) {
        PositionDto pos = new PositionDto(current.x() + xDir, current.y() + yDir);

        if (pos.x() < 0 || pos.x() >= board.width() || pos.y() < 0 || pos.y() >= board.height()) {
            return new ArrayList<>();
        }

        List<Piece> pieces = board.pieces().stream().filter(p -> p.getPosition().equals(pos))
                .toList();

        if (pieces.size() > 1) {
            throw new IllegalStateException("More than one piece on position " + pos);
        }

        if (!pieces.isEmpty()) {
            Piece piece = pieces.getFirst();

            if (this.color == piece.getColor()) {
                return new ArrayList<>();
            }
            return List.of(new MoveDto(this.position, pos, this, MoveFlag.CAPTURE, null));
        }

        List<MoveDto> result = new ArrayList<>();
        result.add(new MoveDto(this.position, pos, this, MoveFlag.NORMAL, null));
        result.addAll(rekursiveStep(pos, board, xDir, yDir));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Piece piece) {
            return piece.getPosition().equals(position)
                    && piece.getColor().equals(color)
                    && piece.getType().equals(type);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(position, color, type);
    }
}
