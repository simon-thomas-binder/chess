/*
package com.example.backend.Service;

import com.example.backend.enums.Color;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class GameState {
    long gameId;
    int width, height;
    Map<String, Piece> pieces; // key = pieceId
    Color turn; // WHITE/BLACK
    long whiteTimeMs, blackTimeMs;
    long lastServerTs; // für Clock
    // optional: Zobrist hash, repetition, castling rights, en-passant, etc.
}

@Component
@RequiredArgsConstructor
public class GameManager {
    private final ConcurrentHashMap<Long, GameState> cache = new ConcurrentHashMap<>();
    private final GameMoveRepository moves;
    private final SimpMessagingTemplate ws;

    public GameState loadOrBuild(long gameId) {
        return cache.computeIfAbsent(gameId, id -> rebuildFromDb(id));
    }

    private GameState rebuildFromDb(long gameId) {
        // 1) Startposition aufbauen
        GameState s = initialStateFromGame(gameId);
        // 2) Alle Moves aus DB anwenden
        for (GameMove m : moves.findByGameIdOrderByMoveNumberAsc(gameId)) {
            apply(s, m);
        }
        return s;
    }

    public synchronized MoveResult processMove(long gameId, MoveDto dto, String username) {
        GameState s = loadOrBuild(gameId);
        // 1) validate: turn, legal move, not leaving king in check, etc.
        // 2) clocks: subtract elapsed from active side, add increment after move
        // 3) mutate state: move piece, capture, promotion, castling, en-passant...
        // 4) persist DB (GameMove)
        // 5) broadcast
        ws.convertAndSend("/topic/game/" + gameId, Map.of(
                "type", "MOVE_APPLIED",
                "payload", mapForClient(s, dto) // include clocks, next turn, optional pieces snapshot
        ));
        return new MoveResult(*/
/*...*//*
);
    }

    public void postChat(long gameId, String username, String text) {
        ws.convertAndSend("/topic/game/" + gameId, Map.of(
                "type","CHAT",
                "payload", Map.of("id",System.nanoTime(),"sender",username,"text",text)
        ));
    }
}
*/
