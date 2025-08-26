package com.example.backend.REST;

import com.example.backend.Service.GameSessionService;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.PositionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/chess/session")
@RequiredArgsConstructor
public class SessionEndpoint {

    private final GameSessionService gameSessionService;

    @GetMapping("/{id}")
    public ResponseEntity<Collection<MoveDto>> getMoves(@PathVariable long id, @RequestParam int x, @RequestParam int y) {
        PositionDto pos = new PositionDto(x, y);
        return new ResponseEntity<>(gameSessionService.getMoves(id, pos), HttpStatus.OK);
    }

    @PostMapping("/{id}")
    public ResponseEntity<Void> playMove(@PathVariable long id, @RequestBody MoveDto moveDto) {
        gameSessionService.playMove(id, moveDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{id}/resign")
    public ResponseEntity<Void> resign(@PathVariable long id) {
        gameSessionService.resign(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //TODO: Remi endpoint

    @PostMapping("/{id}/msg")
    public ResponseEntity<Void> msg(@PathVariable long id, @RequestBody String msg) {
        gameSessionService.msg(id, msg);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
