//Piece Colors
export type Color = "WHITE" | "BLACK";

//Types of pieces
export type PieceType = "KING" | "QUEEN" | "KNIGHT" | "BISHOP" | "ROOK" | "PAWN";

//Chessboard structure
export interface Chessboard {
    width: number;
    height: number;
    initial_time: number; // in ms
    increment: number; // in ms
    delay: number; // in ms
    pieces: Piece[];
}

//Piece structure
export interface Piece {
    type: PieceType;
    xPos: number; // horizontal
    yPos: number; // vertical
    color: Color
}

//Standard chess board
export const initialChessboard: Chessboard = {
    width: 8,
    height: 8,
    initial_time: 600000,
    increment: 1000,
    delay: 0,
    pieces: [
        { type: "ROOK", xPos: 0, yPos: 0, color: "WHITE" },
        { type: "KNIGHT", xPos: 1, yPos: 0, color: "WHITE" },
        { type: "BISHOP", xPos: 2, yPos: 0, color: "WHITE" },
        { type: "QUEEN", xPos: 3, yPos: 0, color: "WHITE" },
        { type: "KING", xPos: 4, yPos: 0, color: "WHITE" },
        { type: "BISHOP", xPos: 5, yPos: 0, color: "WHITE" },
        { type: "KNIGHT", xPos: 6, yPos: 0, color: "WHITE" },
        { type: "ROOK", xPos: 7, yPos: 0, color: "WHITE" },
        { type: "PAWN", xPos: 0, yPos: 1, color: "WHITE" },
        { type: "PAWN", xPos: 1, yPos: 1, color: "WHITE" },
        { type: "PAWN", xPos: 2, yPos: 1, color: "WHITE" },
        { type: "PAWN", xPos: 3, yPos: 1, color: "WHITE" },
        { type: "PAWN", xPos: 4, yPos: 1, color: "WHITE" },
        { type: "PAWN", xPos: 5, yPos: 1, color: "WHITE" },
        { type: "PAWN", xPos: 6, yPos: 1, color: "WHITE" },
        { type: "PAWN", xPos: 7, yPos: 1, color: "WHITE" },
        { type: "ROOK", xPos: 0, yPos: 7, color: "BLACK" },
        { type: "KNIGHT", xPos: 1, yPos: 7, color: "BLACK" },
        { type: "BISHOP", xPos: 2, yPos: 7, color: "BLACK" },
        { type: "QUEEN", xPos: 3, yPos: 7, color: "BLACK" },
        { type: "KING", xPos: 4, yPos: 7, color: "BLACK" },
        { type: "BISHOP", xPos: 5, yPos: 7, color: "BLACK" },
        { type: "KNIGHT", xPos: 6, yPos: 7, color: "BLACK" },
        { type: "ROOK", xPos: 7, yPos: 7, color: "BLACK" },
        { type: "PAWN", xPos: 0, yPos: 6, color: "BLACK" },
        { type: "PAWN", xPos: 1, yPos: 6, color: "BLACK" },
        { type: "PAWN", xPos: 2, yPos: 6, color: "BLACK" },
        { type: "PAWN", xPos: 3, yPos: 6, color: "BLACK" },
        { type: "PAWN", xPos: 4, yPos: 6, color: "BLACK" },
        { type: "PAWN", xPos: 5, yPos: 6, color: "BLACK" },
        { type: "PAWN", xPos: 6, yPos: 6, color: "BLACK" },
        { type: "PAWN", xPos: 7, yPos: 6, color: "BLACK" },
    ]
};
