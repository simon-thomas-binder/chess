package com.example.backend.dto.Game;

import com.example.backend.enums.Color;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Player {
    private final String username;
    private final Color color;
    private long remainingTime;

    public Player(String username, Color color, long remainingTime) {
        this.username = username;
        this.color = color;
        this.remainingTime = remainingTime;
    }
}
