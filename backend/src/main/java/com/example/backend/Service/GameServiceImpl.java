package com.example.backend.Service;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.CreateGameDto;
import com.example.backend.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final UserService userService;
    private final GameSessionService gameSessionService;

    private final Map<Chessboard, Queue<User>> queue = new ConcurrentHashMap<>();

    @Override
    public long createGame(CreateGameDto createGameDto) {
        //TODO
        return 0;
    }

    @Override
    public void joinGame(long gameId) {
        //TODO
    }

    @Override
    public void declineGame(long gameId) {
        //TODO
    }

    @Override
    public void queueGame(Chessboard chessboard) {
        log.info("queueGame {}", chessboard);
        User user1 = userService.getUser();

        dequeueUserFromAll(user1);

        if (!queue.containsKey(chessboard)) {
            queue.put(chessboard, new ConcurrentLinkedDeque<>());
        }

        if (queue.get(chessboard).isEmpty()) {
            queue.get(chessboard).add(user1);
            return;
        }

        User user2 = queue.get(chessboard).poll();

        if (user2 != null) {
            gameSessionService.createGameSession(chessboard, List.of(user1, user2));
        } else {
            queue.get(chessboard).add(user1);
        }
    }

    @Override
    public void dequeueGame() {
        dequeueUserFromAll(userService.getUser());
    }

    @Override
    public void quitGame(long gameId) {
        //TODO
    }

    @Override
    public Object viewGame(long gameId) {
        //TODO
        return null;
    }

    @Override
    public void deleteGame(long gameId) {
        //TODO
    }

    private void dequeueUserFromAll(User user) {
        queue.values().forEach(dq -> {
            dq.removeIf(u -> Objects.equals(u, user));
        });
    }
}
