package com.example.backend.dto.Game;

import com.example.backend.enums.Color;

import java.time.Instant;

public record ChatDetailDto(String msg, Color sender, Instant timestamp) {
}
