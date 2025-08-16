package com.example.backend.Service;

import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.CreateGameDto;
import com.example.backend.entity.Game;
import com.example.backend.entity.GameParticipant;
import com.example.backend.entity.User;
import com.example.backend.enums.Color;
import com.example.backend.enums.GameStatus;
import com.example.backend.repository.GameParticipantRepository;
import com.example.backend.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final GameParticipantRepository gameParticipantRepository;
    private final UserService userService;
    private final SimpMessagingTemplate ws;


    private final Map<Chessboard, Queue<User>> queue = new ConcurrentHashMap<>();
    private static final Random random = new Random();

    @Override
    public long createGame(CreateGameDto createGameDto) {
        return 0;
    }

    @Override
    public void joinGame(long gameId) {

    }

    @Override
    public void declineGame(long gameId) {

    }

    @Override
    public void queueGame(Chessboard chessboard) {
        User user1 = userService.getUser();

        dequeueUserFromAll(user1);

        if (queue.containsKey(chessboard) && !queue.get(chessboard).isEmpty()) {
            User user2 = queue.get(chessboard).poll();

            Map<User, Color> participants = new HashMap<>();
            if (random.nextBoolean()) {
                participants.put(user1, Color.WHITE);
                participants.put(user2, Color.BLACK);
            } else {
                participants.put(user1, Color.BLACK);
                participants.put(user2, Color.WHITE);
            }
            createGame(chessboard, participants);
        } else {
            queue.put(chessboard, new ArrayDeque<>(List.of(user1)));
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

    private void createGame(Chessboard chessboard, Map<User, Color> users) {
        log.info("Creating new game");
        log.debug("Chessboard: {}", chessboard);
        log.debug("Users: {}", users);

        Game game = new Game();
        game.setBoardHeight(chessboard.height());
        game.setBoardWidth(chessboard.width());
        game.setIncrement(chessboard.increment());
        game.setDelay(chessboard.delay());
        game.setInitialTime(chessboard.initial_time());
        game.setStatus(GameStatus.PENDING);

        System.out.println(game.getStatus());

        Game savedGame = gameRepository.save(game);

        for (User user : users.keySet()) {
            GameParticipant participant = new GameParticipant();
            participant.setGame(savedGame);
            participant.setUser(user);
            participant.setColor(users.get(user));
            gameParticipantRepository.save(participant);

            ws.convertAndSend("/topic/user/" + user.getUsername(), Map.of(
                    "type", "MATCH_FOUND",
                    "payload", Map.of(
                            "gameId", savedGame.getId(),
                            "color", users.get(user),
                            "opponents", users.entrySet().stream()
                                    .filter(entry -> !entry.getKey().equals(user))
                                    .map(entry -> Map.of("username", entry.getKey().getUsername(), "color", entry.getValue()))
                                    .toList()
                    )
            ));
        }
    }

    private void dequeueUserFromAll(User user) {
        queue.values().forEach(dq -> {
            dq.removeIf(u -> Objects.equals(u, user));
        });
    }
}
