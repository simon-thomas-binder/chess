package com.example.backend.Service;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.PositionDto;
import com.example.backend.entity.User;
import com.example.backend.exception.ConflictException;
import com.example.backend.repository.GameParticipantRepository;
import com.example.backend.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameSessionServiceImpl implements GameSessionService {

    private final GameRepository gameRepository;
    private final GameParticipantRepository gameParticipantRepository;
    private final SimpMessagingTemplate ws;
    private final UserService userService;

    private final Map<Long, GameSession> gameSessions = new ConcurrentHashMap<>();

    @Override
    public void createGameSession(Chessboard board, List<User> users) {
        log.info("Creating game session");
        GameSession session = new GameSession(board, users, gameRepository, gameParticipantRepository, ws);
        long id = session.getGameId();

        gameSessions.put(id, session);
    }

    @Override
    public Collection<MoveDto> getMoves(long gameId, PositionDto pos) {
        GameSession session = gameSessions.get(gameId);
        if (session == null) {
            throw new ConflictException("Game session does not exist");
        }

        return session.getMoves(pos, userService.getUser().getUsername());
    }

    @Override
    public void playMove(long gameId, MoveDto moveDto) {

    }
}
