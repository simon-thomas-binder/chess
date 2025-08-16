import api from "../api";
import type {Chessboard} from "../models/chess.ts";

export async function queueChessboard(cb: Chessboard) {
    return api.post("/chess/queue", {
        chessboard: { width: cb.width, height: cb.height },
        initial_time: cb.initial_time,
        increment: cb.increment,
        delay: cb.delay
    });
}

export async function createGame(cb: Chessboard) {
    return api.post("/chess/create", {
        chessboard: { width: cb.width, height: cb.height },
        initial_time: cb.initial_time,
        increment: cb.increment,
        delay: cb.delay
    });
}

export type MovePayload = {
    from: { row: number; col: number };
    to: { row: number; col: number };
    promotion?: "queen"|"rook"|"bishop"|"knight";
};

export async function sendMove(gameId: string|number, move: MovePayload) {
    return api.post(`/games/${gameId}/moves`, move);
}

export async function offerDraw(gameId: string|number) {
    return api.post(`/games/${gameId}/draw`);
}

export async function resign(gameId: string|number) {
    return api.post(`/games/${gameId}/resign`);
}

export async function sendChat(gameId: string|number, text: string) {
    return api.post(`/games/${gameId}/chat`, { text });
}

export async function getInitialState(gameId: string|number) {
    return api.get(`/games/${gameId}`);
}
