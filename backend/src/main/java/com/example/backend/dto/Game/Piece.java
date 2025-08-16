package com.example.backend.dto.Game;

import com.example.backend.enums.Color;
import com.example.backend.enums.PieceType;

public record Piece(PieceType type, int xPos, int yPos, Color color) {
}
