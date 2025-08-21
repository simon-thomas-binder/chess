package com.example.backend.Service;
import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.PositionDto;
import com.example.backend.dto.Game.piece.Piece;
import com.example.backend.entity.Game;
import com.example.backend.entity.GameParticipant;
import com.example.backend.entity.Move;
import com.example.backend.entity.Position;
import com.example.backend.entity.User;
import com.example.backend.enums.Color;
import com.example.backend.enums.GameStatus;
import com.example.backend.exception.NotAuthorizedException;
import com.example.backend.repository.GameParticipantRepository;
import com.example.backend.repository.GameRepository;
import com.example.backend.repository.MoveRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class GameSession {

    private final GameRepository gameRepository;
    private final MoveRepository moveRepository;
    private final SimpMessagingTemplate ws;

    private final Chessboard board;
    private final List<Player> players = new ArrayList<>();

    private Color turn = Color.WHITE;
    private boolean gameOver = false;

    private int ply = 0;

    private Game game;
    private Long lastTurnStartMs = null;

    public GameSession(Chessboard board, List<User> users,
                       GameRepository gameRepository,
                       GameParticipantRepository gameParticipantRepository,
                       SimpMessagingTemplate ws,
                       MoveRepository moveRepository) {
        this.gameRepository = gameRepository;
        this.ws = ws;
        this.moveRepository = moveRepository;

        this.board = board;

        game = new Game();
        game.setBoardHeight(board.height());
        game.setBoardWidth(board.width());
        game.setIncrement(board.increment());
        game.setDelay(board.delay());
        game.setInitialTime(board.initial_time());
        game.setStatus(GameStatus.PENDING);
        safe(game);

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

        for (Player player : players) {
            ws.convertAndSend("/topic/user/" + player.getUsername(), Map.of(
                    "type", "MATCH_FOUND",
                    "payload", Map.of(
                            "gameId", game.getId(),
                            "color", player.getColor(),
                            "opponents", players.stream()
                                    .filter(entry -> !entry.equals(player))
                                    .map(entry -> Map.of("username", entry.getUsername(), "color", entry.getColor()))
                                    .toList(),
                            "chessboard", board
                    )
            ));
        }
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

        return piece.getMoves(board);
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
            player.setRemainingTime(player.getRemainingTime() - (lastTurnStartMs - System.currentTimeMillis()) + board.delay());
        } else {
            game.setStatus(GameStatus.ACTIVE);
            safe(game);
        }

        lastTurnStartMs = System.currentTimeMillis();

        Move moveEntity = new Move();
        moveEntity.setGame(game);
        moveEntity.setFrom(new Position(move.from().x(), move.from().y()));
        moveEntity.setTo(new Position(move.to().x(), move.to().y()));
        moveEntity.setMove(move.flag());
        moveEntity.setActor(player.getColor());
        moveEntity.setPiece(move.piece().getType());
        moveEntity.setPly(ply);
        moveEntity.setPromotionTo(move.promotionTo());
        moveEntity.setServerReceivedAt(Instant.now());
        moveEntity.setActorTimeLeftMsAfter(player.getRemainingTime());

        moveRepository.save(moveEntity);

        Map<String, Object> message = new HashMap<>();

        message.put("type", "MOVE_APPLIED");

        Map<String, Object> moveDetails = new HashMap<>();
        moveDetails.put("from", Map.of("x", move.from().x(), "y", move.from().y()));
        moveDetails.put("to", Map.of("x", move.to().x(), "y", move.to().y()));
        moveDetails.put("piece", move.piece());
        moveDetails.put("flag", move.flag().name());
        moveDetails.put("promotionTo", move.promotionTo() != null ? move.promotionTo().name() : null);

        message.put("move", moveDetails);
        message.put("currentTurn", turn.name());

        Map<String, Long> remainingTime = new HashMap<>();
        for (Player p : players) {
            remainingTime.put(p.getColor().name().toLowerCase(), p.getRemainingTime());
        }

        message.put("remainingTime", remainingTime);
        message.put("status", game.getStatus().name());

        ws.convertAndSend("/topic/game/" + getGameId(), message);

        board.move(move);
        ply++;
        player.setRemainingTime(player.getRemainingTime() + board.increment());

        if (turn == Color.WHITE) {
            turn = Color.BLACK;
        } else {
            turn = Color.WHITE;
        }
    }

    private void safe(Game game) {
        this.game = gameRepository.save(game);
    }
}

@Getter @Setter
class Player {
    private final String username;
    private final Color color;
    private long remainingTime;

    Player(String username, Color color, long remainingTime) {
        this.username = username;
        this.color = color;
        this.remainingTime = remainingTime;
    }
}