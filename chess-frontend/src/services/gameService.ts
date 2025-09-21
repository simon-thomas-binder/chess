import api from "../api";
import type {Chessboard, Move, Position} from "../models/chess.ts";

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

export async function getMoves(id: number | undefined, pos: Position) : Promise<Move[]>  {
    const params = {
        x: pos.x,
        y: pos.y
    };
    const response = await api.get(`/chess/session/` + id, {params})
    return response.data;
}

export async function sendMove(gameId: string|number, move: Move) {
    return api.post(`/chess/session/${gameId}`, move);
}

export async function offerDraw(gameId: string|number) {
    return api.post(`/games/${gameId}/draw`);
}

export async function resign(gameId: string|number) {
    return api.post(`/chess/session/${gameId}/resign`);
}

export async function sendChat(gameId: string|number, text: string) {
    return api.post(`/chess/session/${gameId}/msg`, { text });
}
