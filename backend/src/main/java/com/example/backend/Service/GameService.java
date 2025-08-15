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

    void queueGame(Chessboard chessboard);

    void dequeueGame();

    void quitGame(long gameId);

    Object viewGame(long gameId);

    void deleteGame(long gameId);
}
