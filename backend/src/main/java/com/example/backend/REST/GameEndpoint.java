package com.example.backend.REST;

import com.example.backend.Service.GameService;
import com.example.backend.dto.Game.Chessboard;
import com.example.backend.dto.Game.CreateGameDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chess")
@RequiredArgsConstructor
public class GameEndpoint {

    private final GameService gameService;

    @PostMapping("/create")
    public ResponseEntity<Long> createGame (@Valid @RequestBody CreateGameDto gameDto) {
        return new ResponseEntity<>(gameService.createGame(gameDto), HttpStatus.CREATED);
    }

    @PostMapping("/join/{id}")
    public ResponseEntity<Void> joinGame (@PathVariable long id) {
        gameService.joinGame(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/join/{id}")
    public ResponseEntity<Void> declineGame (@PathVariable long id) {
        gameService.declineGame(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/queue")
    public ResponseEntity<Void> queueGame (@Valid @RequestBody Chessboard chessboard) {
        gameService.queueGame(chessboard);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/queue")
    public ResponseEntity<Void> dequeueGame () {
        gameService.dequeueGame();
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/quit/{id}")
    public ResponseEntity<Void> quitGame (@PathVariable long id) {
        gameService.quitGame(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<?> viewGame (@PathVariable long id) {
        gameService.viewGame(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteGame (@PathVariable long id) {
        gameService.deleteGame(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
