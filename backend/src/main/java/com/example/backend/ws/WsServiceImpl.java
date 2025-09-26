package com.example.backend.ws;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.Player;
import com.example.backend.enums.Color;
import com.example.backend.enums.GameEndFlag;
import com.example.backend.enums.GameStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WsServiceImpl implements WsService {

    private final SimpMessagingTemplate ws;
    private final long gameId;

    public WsServiceImpl(SimpMessagingTemplate ws, long gameId) {
        this.ws = ws;
        this.gameId = gameId;
    }

    private void sendMessage(String destination, Map<String, Object> details, WsTypes type) {
        Map<String, Object> message = new HashMap<>();
        message.put("type", type);
        message.put("details", details);
        ws.convertAndSend("/topic/" + destination, message);
    }

    @Override
    public void sendMatchFound(List<Player> players, Chessboard board) {
        for (Player player : players) {
            Map<String, Object> details = Map.of(
                    "gameId", gameId,
                    "color", player.color(),
                    "opponents", players.stream()
                            .filter(entry -> !entry.equals(player))
                            .map(entry -> Map.of("username", entry.username(), "color", entry.color()))
                            .toList(),
                    "chessboard", board
            );
            sendMessage("user/" + player.username(), details, WsTypes.MATCH_FOUND);
        }
    }

    @Override
    public void sendMoveApplied(MoveDto move, Color turn, Map<Color, Long> times, GameStatus status) {
        Map<String, Object> details = new HashMap<>();

        Map<String, Object> moveDetails = new HashMap<>();
        moveDetails.put("from", Map.of("x", move.from().x(), "y", move.from().y()));
        moveDetails.put("to", Map.of("x", move.to().x(), "y", move.to().y()));
        moveDetails.put("piece", move.piece());
        moveDetails.put("flag", move.flag());
        moveDetails.put("promotionTo", move.promotionTo() != null ? move.promotionTo() : null);

        details.put("move", moveDetails);
        details.put("currentTurn", turn);
        Map<String, Long> remainingTime = new HashMap<>();
        for (Color c : times.keySet()) {
            remainingTime.put(c.toString(), times.get(c));
        }
        details.put("remainingTime", remainingTime);
        details.put("status", status);

        sendMessage("game/" + gameId, details, WsTypes.MOVE_APPLIED);
    }

    @Override
    public void sendGameEnded(GameEndFlag flag, Color winner) {
        Map<String, Object> details = new HashMap<>();
        details.put("winner", winner);
        details.put("endFlag", flag);
        sendMessage("game/" + gameId, details, WsTypes.GAME_ENDED);
    }

    @Override
    public void sendChatEvent(String msg, Color color, Instant time) {
        Map<String, Object> details = new HashMap<>();
        details.put("color", color);
        details.put("msg", msg);
        details.put("time", time);
        sendMessage("game/" + gameId, details, WsTypes.CHAT);
    }

    @Override
    public void sendDrawEvent(String details) {
        Map<String, Object> d = new HashMap<>();
        d.put("flag", details);
        sendMessage("game/" + gameId, d, WsTypes.DRAW_EVENT);
    }
}
