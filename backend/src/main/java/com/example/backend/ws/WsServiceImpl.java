package com.example.backend.ws;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.Player;
import com.example.backend.enums.Color;
import com.example.backend.enums.GameEndFlag;
import com.example.backend.enums.GameStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WsServiceImpl implements WsService{

    private final SimpMessagingTemplate ws;
    private final long gameId;

    public WsServiceImpl(SimpMessagingTemplate ws, long gameId) {
        this.ws = ws;
        this.gameId = gameId;
    }

    @Override
    public void sendMatchFound(List<Player> players, Chessboard board) {
        for (Player player : players) {
            ws.convertAndSend("/topic/user/" + player.getUsername(), Map.of(
                    "type", "MATCH_FOUND",
                    "payload", Map.of(
                            "gameId", gameId,
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

    @Override
    public void sendMoveApplied(MoveDto move, Color turn, List<Player> players, GameStatus status) {
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
        message.put("status", status);

        ws.convertAndSend("/topic/game/" + gameId, message);
    }

    @Override
    public void sendGameEnded(GameEndFlag flag, Color winner) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", "GAME_ENDED");

        Map<String, Object> endDetails = new HashMap<>();
        endDetails.put("winner", winner.name());
        endDetails.put("endFlag", flag.name());

        message.put("details", endDetails);

        ws.convertAndSend("/topic/game/" + gameId, message);
    }
}
