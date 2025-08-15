import api from "../api";

export interface ChessboardPayload {
    width: number;
    height: number;
    initial_time: number; // in ms
    increment: number; // in ms
    delay: number; // in ms
}

export async function queueChessboard(cb: ChessboardPayload) {
    return api.post("/chess/queue", {
        chessboard: { width: cb.width, height: cb.height },
        initial_time: cb.initial_time,
        increment: cb.increment,
        delay: cb.delay
    });
}

export async function createGame(cb: ChessboardPayload) {
    return api.post("/chess/create", {
        chessboard: { width: cb.width, height: cb.height },
        initial_time: cb.initial_time,
        increment: cb.increment,
        delay: cb.delay
    });
}
