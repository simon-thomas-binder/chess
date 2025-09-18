// Piece Colors
export type Color = "WHITE" | "BLACK";

// Types of pieces
export type PieceType = "KING" | "QUEEN" | "KNIGHT" | "BISHOP" | "ROOK" | "PAWN";

export type MoveFlag = "NORMAL" | "CAPTURE" | "CASTLE_KING" | "CASTLE_QUEEN" | "PROMOTION" | "EN_PASSANT"

export interface Cell {
    key: string;
    x: number;
    y: number;
    piece: Piece | null;
    dark: boolean;
    sel: boolean;   // selection flag
    hl: boolean;    // highlight flag
}

//Chessboard structure
export interface Chessboard {
    width: number;
    height: number;
    initial_time: number;   // in ms
    increment: number;      // in ms
    delay: number;          // in ms
    pieces: Piece[];
}

//Piece structure
export interface Piece {
    type: PieceType;
    position: Position;
    color: Color;
}

export interface Position {
    x: number;
    y: number;
}

export function positionToString(pos: Position): string {
    return `${pos.x}, ${pos.y}`;
}

export interface Move {
    from: Position;
    to: Position;
    piece: Piece;
    flag: MoveFlag;
    promotionTo: PieceType;
}

//Standard chess board
export const initialChessboard: Chessboard = {
    width: 8,
    height: 8,
    initial_time: 600000,
    increment: 1000,
    delay: 0,
    pieces: [
        { type: "ROOK", position: { x: 0, y: 0 }, color: "WHITE" },
        { type: "KNIGHT", position: { x: 1, y: 0 }, color: "WHITE" },
        { type: "BISHOP", position: { x: 2, y: 0 }, color: "WHITE" },
        { type: "QUEEN", position: { x: 3, y: 0 }, color: "WHITE" },
        { type: "KING", position: { x: 4, y: 0 }, color: "WHITE" },
        { type: "BISHOP", position: { x: 5, y: 0 }, color: "WHITE" },
        { type: "KNIGHT", position: { x: 6, y: 0 }, color: "WHITE" },
        { type: "ROOK", position: { x: 7, y: 0 }, color: "WHITE" },
        { type: "PAWN", position: { x: 0, y: 1 }, color: "WHITE" },
        { type: "PAWN", position: { x: 1, y: 1 }, color: "WHITE" },
        { type: "PAWN", position: { x: 2, y: 1 }, color: "WHITE" },
        { type: "PAWN", position: { x: 3, y: 1 }, color: "WHITE" },
        { type: "PAWN", position: { x: 4, y: 1 }, color: "WHITE" },
        { type: "PAWN", position: { x: 5, y: 1 }, color: "WHITE" },
        { type: "PAWN", position: { x: 6, y: 1 }, color: "WHITE" },
        { type: "PAWN", position: { x: 7, y: 1 }, color: "WHITE" },
        { type: "ROOK", position: { x: 0, y: 7 }, color: "BLACK" },
        { type: "KNIGHT", position: { x: 1, y: 7 }, color: "BLACK" },
        { type: "BISHOP", position: { x: 2, y: 7 }, color: "BLACK" },
        { type: "QUEEN", position: { x: 3, y: 7 }, color: "BLACK" },
        { type: "KING", position: { x: 4, y: 7 }, color: "BLACK" },
        { type: "BISHOP", position: { x: 5, y: 7 }, color: "BLACK" },
        { type: "KNIGHT", position: { x: 6, y: 7 }, color: "BLACK" },
        { type: "ROOK", position: { x: 7, y: 7 }, color: "BLACK" },
        { type: "PAWN", position: { x: 0, y: 6 }, color: "BLACK" },
        { type: "PAWN", position: { x: 1, y: 6 }, color: "BLACK" },
        { type: "PAWN", position: { x: 2, y: 6 }, color: "BLACK" },
        { type: "PAWN", position: { x: 3, y: 6 }, color: "BLACK" },
        { type: "PAWN", position: { x: 4, y: 6 }, color: "BLACK" },
        { type: "PAWN", position: { x: 5, y: 6 }, color: "BLACK" },
        { type: "PAWN", position: { x: 6, y: 6 }, color: "BLACK" },
        { type: "PAWN", position: { x: 7, y: 6 }, color: "BLACK" },
    ]
};

