package com.example.backend.dto.Game;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record Chessboard(@Min(3) @Max(64) int width, @Min(3) @Max(64) int height, @Min(1) int initial_time, int increment, int delay) {
}
