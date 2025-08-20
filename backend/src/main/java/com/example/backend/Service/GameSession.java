package com.example.backend.Service;
import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.PositionDto;
import com.example.backend.dto.Game.piece.Piece;
import com.example.backend.entity.Game;
import com.example.backend.entity.GameParticipant;
import com.example.backend.entity.User;
import com.example.backend.enums.Color;
import com.example.backend.enums.GameStatus;
import com.example.backend.exception.NotAuthorizedException;
import com.example.backend.repository.GameParticipantRepository;
import com.example.backend.repository.GameRepository;
import lombok.Getter;
import lombok.Setter;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class GameSession {

    private final GameRepository gameRepository;
    private final SimpMessagingTemplate ws;

    private final Chessboard board;
    private final List<Player> players = new ArrayList<>();

    private Color turn = Color.WHITE;
    private boolean gameOver = false;

    private Game game;
    private long lastTurnStartMs;

    public GameSession(Chessboard board, List<User> users,
                       GameRepository gameRepository,
                       GameParticipantRepository gameParticipantRepository,
                       SimpMessagingTemplate ws) {
        this.gameRepository = gameRepository;
        this.ws = ws;

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

        Color finalColor = color;
        Piece piece = board.pieces().stream().filter(p -> p.getColor().equals(finalColor))
                .filter(p -> p.getPosition().x() == position.x() && p.getPosition().y() == position.y()).findFirst().orElse(null);

        if (piece == null) {
            return null;
        }

        return piece.getMoves(board);
    }

    public void playMove(PositionDto position, String username) {
        //TODO
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