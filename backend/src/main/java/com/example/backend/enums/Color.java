package com.example.backend.enums;

public enum Color {
    WHITE,
    BLACK;

    public static Color getOtherColor(Color color) {
        return switch (color) {
            case WHITE -> Color.BLACK;
            case BLACK -> Color.WHITE;
        };
    }
}

