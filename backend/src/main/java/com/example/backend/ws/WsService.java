package com.example.backend.ws;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.Player;
import com.example.backend.enums.Color;
import com.example.backend.enums.GameEndFlag;
import com.example.backend.enums.GameStatus;

import java.time.Instant;
import java.util.List;

public interface WsService {

    /**
     * Sends a math found event on all players
     *
     * @param players found for that match
     * @param board of that match
     */
    void sendMatchFound(List<Player> players, Chessboard board);

    /**
     * Sends a move applied event for that game
     *
     * @param move played
     * @param turn who is now on turn
     * @param players of that game
     * @param status of that game
     */
    void sendMoveApplied(MoveDto move, Color turn, List<Player> players, GameStatus status);

    /**
     * Sends a game ending event
     *
     * @param flag reason why the game ended
     * @param winner of the game (can be null!)
     */
    void sendGameEnded(GameEndFlag flag, Color winner);

    /**
     * Sends a chat event by the player with given color
     *
     * @param msg message to send
     * @param color of the player
     */
    void sendChatEvent(String msg, Color color, Instant time);
}
