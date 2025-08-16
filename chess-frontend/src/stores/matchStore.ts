// src/stores/matchStore.ts
import { defineStore } from "pinia";
import type {Chessboard, Color} from "../models/chess.ts";

// Opponent data
type Opp = {
    username: string;
    color: Color
};

// Saves match data from ws
type Match = {
    gameId: number;
    color: Color;
    opponents: Opp[];
    chessboard: Chessboard
};

export const useMatchStore = defineStore("match", {
    state: () => ({
        current: null as Match | null
    }),
    actions: {
        setMatch(m: Match) { this.current = m; },
        clear() { this.current = null; }
    }
});
