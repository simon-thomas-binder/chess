package com.example.backend.Service;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.CreateGameDto;

public interface GameService {

    /**
     * Creates a pending game
     *
     * @param createGameDto game data
     */
    long createGame(CreateGameDto createGameDto);

    /**
     * Joins a game
     *
     * @param gameId if of the game
     */
    void joinGame(long gameId);

    /**
     * Declines a game offer
     *
     * @param gameId id of the game to decline
     */
    void declineGame(long gameId);

    /**
     * Sets this user in the queue for a chess game
     *
     * @param chessboard type of game to be queued for
     */
    void queueGame(Chessboard chessboard);

    /**
     * Dequeues this User
     */
    void dequeueGame();

    //TODO
    void quitGame(long gameId);

    //TODO
    Object viewGame(long gameId);

    //TODO
    void deleteGame(long gameId);
}
