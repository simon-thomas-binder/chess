package com.example.backend.REST;

import com.example.backend.Service.GameSessionService;
import com.example.backend.dto.Game.MoveDto;
import com.example.backend.dto.Game.PositionDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;

@RestController
@RequestMapping("/api/chess/move")
@RequiredArgsConstructor
public class MoveEndpoint {

    private final GameSessionService gameSessionService;

    @GetMapping("/{id}")
    public Collection<MoveDto> getMoves(@PathVariable long id, @RequestParam int x, @RequestParam int y) {
        PositionDto pos = new PositionDto(x, y);
        return gameSessionService.getMoves(id, pos);
    }

    @PostMapping("/{id}")
    public void playMove(@PathVariable long id, @RequestBody MoveDto moveDto) {
        //TODO
    }
}
