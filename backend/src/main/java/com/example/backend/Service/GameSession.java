package com.example.backend.Service;
import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.Player;
import com.example.backend.dto.Game.PositionDto;
import com.example.backend.dto.Game.piece.Piece;
import com.example.backend.entity.Game;
import com.example.backend.entity.GameParticipant;
import com.example.backend.entity.Move;
import com.example.backend.entity.User;
import com.example.backend.enums.Color;
import com.example.backend.enums.GameEndFlag;
import com.example.backend.enums.GameStatus;
import com.example.backend.exception.NotAuthorizedException;
import com.example.backend.exception.ValidationException;
import com.example.backend.repository.GameParticipantRepository;
import com.example.backend.repository.GameRepository;
import com.example.backend.repository.MoveRepository;
import com.example.backend.ws.WsService;
import com.example.backend.ws.WsServiceImpl;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * GameSession objects that stores data for the chess game
 */
public class GameSession {

    // Scheduler fot the game clock
    private static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2), r -> {
                Thread t = new Thread(r, "game-clock");
                t.setDaemon(true);
                return t;
            });

    private ScheduledFuture<?> currentTimerTask;

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final WsService wsService;

    private final Chessboard board;
    private final List<Player> players = new ArrayList<>();

    private Color turn = Color.WHITE;
    private boolean gameOver = false;

    private int ply = 0;

    private Game game;
    private Long lastTurnStartMs = null;

    // Constructor
    public GameSession(Chessboard board, List<User> users,
                       GameRepository gameRepository,
                       GameParticipantRepository gameParticipantRepository,
                       SimpMessagingTemplate ws,
                       MoveRepository moveRepository) {
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
        this.board = board;
        this.game = gameRepository.save(Game.newGame(board));
        this.wsService = new WsServiceImpl(ws, game.getId());

        Queue<Color> colors = new ArrayDeque<>(Arrays.asList(Color.WHITE, Color.BLACK));

        for (User user : users) {
            Color color = colors.poll();

            if (color == null) {
                throw new IllegalStateException("User " + user + " has no color");
            }

            players.add(new Player(user.getUsername(), color, board.initial_time()));

            GameParticipant participant = new GameParticipant();
            participant.setGame(game);
            participant.setUser(user);
            participant.setColor(color);
            gameParticipantRepository.save(participant);
        }
        wsService.sendMatchFound(players, board);
    }

    public long getGameId() {
        return game.getId();
    }

    public Collection<MoveDto> getMoves(PositionDto position, String username) {
        Color color = null;

        for (Player player : players) {
            if (player.getUsername().equals(username)) {
                color = player.getColor();
            }
        }

        if (color == null) {
            throw new NotAuthorizedException("You are no player in this game");
        }

        if (color != turn) {
            return new ArrayList<>();
        }

        Color finalColor = color;
        Piece piece = board.pieces().stream().filter(p -> p.getColor().equals(finalColor))
                .filter(p -> p.getPosition().x() == position.x() && p.getPosition().y() == position.y()).findFirst().orElse(null);

        if (piece == null) {
            return null;
        }

        return piece.getMoves(board, true);
    }

    public synchronized void playMove(MoveDto move, String username) {

        if (gameOver) {
            throw new NotAuthorizedException("This game has already ended");
        }

        Collection<MoveDto> moves = this.getMoves(move.from(), username);

        if (!moves.contains(move)) {
            throw new NotAuthorizedException("You are not authorized to play this move");
        }

        Player player = players.stream().filter(p -> p.getUsername().equals(username)).findFirst().orElse(null);

        if (player == null) {
            throw new IllegalStateException("This should not happen, player is null");
        }

        if (lastTurnStartMs != null) {
            player.setRemainingTime(player.getRemainingTime() - (System.currentTimeMillis() - lastTurnStartMs) + board.delay());
        } else {
            game.setStatus(GameStatus.ACTIVE);
            safe(game);
        }

        lastTurnStartMs = System.currentTimeMillis();
        moveRepository.save(Move.newMove(move, game, player, ply));
        wsService.sendMoveApplied(move, turn, players, game.getStatus());

        board.move(move);
        ply++;
        player.setRemainingTime(player.getRemainingTime() + board.increment());

        turn = Color.getOtherColor(turn);
        gameEndCheck();

        timer(players.stream().filter(p -> p.getColor().equals(turn)).findFirst().orElseThrow().getRemainingTime());
    }

    /**
     * Resigns the game
     *
     * @param username of the player surrendering
     */
    public void resign(String username) {
        if (gameOver) {
            throw new ValidationException("This game has already ended");
        }
        Player player = players.stream().filter(p -> p.getUsername().equals(username)).findFirst().orElse(null);
        if (player == null) {
            throw new NotAuthorizedException("You are not player in this game");
        }
        players.remove(player);
        if (players.size() == 1) {
            Color winner = players.getFirst().getColor();
            safe(Game.endGame(game, GameEndFlag.RESIGNATION, winner));
            gameOver = true;
            wsService.sendGameEnded(GameEndFlag.RESIGNATION, winner);
        }
    }

    private void safe(Game game) {
        this.game = gameRepository.save(game);
    }

    // timer restart function
    private synchronized void timer(long time) {

        if (currentTimerTask != null) {
            currentTimerTask.cancel(false);
        }

        currentTimerTask = SCHEDULER.schedule(this::timeUp, time, TimeUnit.MILLISECONDS);
    }

    // trigger for when the time for a player has run out
    private void timeUp() {
        Color color = this.turn;

        players.remove(players.stream().filter(p -> p.getColor().equals(color)).findFirst().orElse(null));
        if (players.size() == 1) {
            Color winner = players.getFirst().getColor();
            safe(Game.endGame(game, GameEndFlag.TIMEOUT, winner));
            gameOver = true;
            wsService.sendGameEnded(GameEndFlag.TIMEOUT, winner);
        }
    }

    private void gameEndCheck() {
        if (board.getAllMoves(turn).isEmpty()) {
            gameOver = true;
            if (board.isInCheck(turn)) {
                Color winner = Color.getOtherColor(turn);
                safe(Game.endGame(game, GameEndFlag.CHECKMATE, winner));
                wsService.sendGameEnded(GameEndFlag.CHECKMATE, winner);
            } else {
                safe(Game.endGame(game, GameEndFlag.STALEMATE, null));
                wsService.sendGameEnded(GameEndFlag.STALEMATE, null);
            }
        }
    }
}