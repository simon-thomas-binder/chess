package com.example.backend.Service;
import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.Player;
import com.example.backend.dto.Game.PositionDto;
import com.example.backend.dto.Game.piece.Piece;
import com.example.backend.entity.ChatMessage;
import com.example.backend.entity.Game;
import com.example.backend.entity.GameParticipant;
import com.example.backend.entity.Move;
import com.example.backend.entity.User;
import com.example.backend.enums.Color;
import com.example.backend.enums.GameEndFlag;
import com.example.backend.enums.GameStatus;
import com.example.backend.exception.NotAuthorizedException;
import com.example.backend.exception.ValidationException;
import com.example.backend.repository.ChatMessageRepository;
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
import java.util.Map;
import java.util.Queue;

/**
 * GameSession objects that stores data for the chess game
 */
public class GameSession {

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final WsService wsService;

    private final TimeControl timeControl;
    private final Chessboard board;
    private final List<Player> players = new ArrayList<>();

    private Color turn = Color.WHITE;
    private boolean gameOver = false;

    private int ply = 0;

    private Game game;

    // Constructor
    public GameSession(Chessboard board, List<User> users,
                       GameRepository gameRepository,
                       GameParticipantRepository gameParticipantRepository,
                       SimpMessagingTemplate ws,
                       MoveRepository moveRepository) {
        this.gameRepository = gameRepository;
        this.moveRepository = moveRepository;
        this.board = board;
        this.game = gameRepository.save(new Game(board));
        this.wsService = new WsServiceImpl(ws, game.getId());

        Queue<Color> colors = new ArrayDeque<>(Arrays.asList(Color.WHITE, Color.BLACK));
        this.timeControl = new TimeControl(this, board.initial_time(), board.increment(), board.delay(), colors.stream().toList());
        for (User user : users) {
            Color color = colors.poll();

            if (color == null) {
                throw new IllegalStateException("User " + user + " has no color");
            }

            players.add(new Player(user.getUsername(), color));
            gameParticipantRepository.save(new GameParticipant(game, user, color));
        }
        wsService.sendMatchFound(players, board);
    }

    public long getGameId() {
        return game.getId();
    }

    public Collection<MoveDto> getMoves(PositionDto position, String username) {
        gameOverValidation();
        Color color = getPlayer(username).color();

        if (color != turn) {
            return new ArrayList<>();
        }

        Piece piece = board.pieces().stream().filter(p -> p.getColor().equals(color))
                .filter(p -> p.getPosition().x() == position.x() && p.getPosition().y() == position.y()).findFirst().orElse(null);

        if (piece == null) {
            return null;
        }

        return piece.getMoves(board, true);
    }

    public synchronized void playMove(MoveDto move, String username) {
        Collection<MoveDto> moves = this.getMoves(move.from(), username);

        if (!moves.contains(move)) {
            throw new NotAuthorizedException("You are not authorized to play this move");
        }

        Player player = getPlayer(username);

        if (game.getStatus() != GameStatus.ACTIVE) {
            game.setStatus(GameStatus.ACTIVE);
            safe(game);
        }

        Map<Color, Long> times = timeControl.onMovePlayed(player.color());
        moveRepository.save(new Move(move, game, player, times.get(player.color()), ply));
        wsService.sendMoveApplied(move, turn, times, game.getStatus());

        board.move(move);
        ply++;

        turn = Color.getOtherColor(turn);
        timeControl.startTurn(turn);
        gameEndCheck();
    }

    /**
     * Resigns the game
     *
     * @param username of the player surrendering
     */
    public void resign(String username) {
        gameOverValidation();
        Player player = getPlayer(username);
        players.remove(player);
        if (players.size() == 1) {
            gameEnd(GameEndFlag.RESIGNATION, players.getFirst().color());
        }
    }

    private void safe(Game game) {
        this.game = gameRepository.save(game);
    }

    public synchronized void timeUp(Color color) {
        players.remove(players.stream().filter(p -> p.color().equals(color)).findFirst().orElse(null));
        if (players.size() == 1) {
            gameEnd(GameEndFlag.TIMEOUT, players.getFirst().color());
        }
    }

    private void gameEndCheck() {
        if (board.getAllMoves(turn).isEmpty()) {
            if (board.isInCheck(turn)) {
                gameEnd(GameEndFlag.CHECKMATE, Color.getOtherColor(turn));
            } else {
                gameEnd(GameEndFlag.STALEMATE, null);
            }
        }
    }

    private void gameEnd(GameEndFlag flag, Color winner) {
        safe(Game.endGame(game, flag, winner));
        gameOver = true;
        wsService.sendGameEnded(flag, winner);
        timeControl.stop();
    }

    public void msg(String msg, String username, ChatMessageRepository repository) {
        Player player = getPlayer(username);
        ChatMessage message = repository.save(new ChatMessage(msg, player.color(), game));
        wsService.sendChatEvent(msg, player.color(), message.getCreatedAt());
    }

    private Player getPlayer(String username) {
        return players.stream().filter(p -> p.username().equals(username)).findFirst().orElseThrow(() -> new ValidationException("You are no player in this game"));
    }

    private void gameOverValidation() {
        if (gameOver) {
            throw new NotAuthorizedException("This game has already ended");
        }
    }
}