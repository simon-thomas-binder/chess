package com.example.backend.Service;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.PositionDto;
import com.example.backend.entity.User;

import java.util.Collection;
import java.util.List;

public interface GameSessionService {

    /**
     * Creates a game session in memory
     *
     * @param board data
     * @param users players
     */
    void createGameSession(Chessboard board, List<User> users);

    /**
     * Get legal moves
     *
     * @param gameId id of the game
     * @param pos position from where to get legal moves
     * @return alle possible legal moves
     */
    Collection<MoveDto> getMoves(long gameId, PositionDto pos);

    /**
     * Play move
     *
     * @param gameId id of the game
     * @param moveDto move to play
     */
    void playMove(long gameId, MoveDto moveDto);

    /**
     * Resigns at given game
     *
     * @param gameId id of the game
     */
    void resign(long gameId);

    /**
     * Offers a draw to the other player
     *
     * @param gameId id of the game
     */
    void offerDraw(long gameId);

    /**
     * Accepts a draw offer of another player
     *
     * @param gameId id of the game
     */
    void acceptDraw(long gameId);

    /**
     * Declines a draw offer of another player
     *
     * @param gameId id of the game
     */
    void declineDraw(long gameId);

    /**
     * Sends a chat message on given game
     *
     * @param gameId id of the game
     * @param msg to send
     */
    void msg(long gameId, String msg);
}
