package com.example.backend.dto;

import java.time.LocalDateTime;

public record ReturnUserDto(String username, String displayname, LocalDateTime createdAt, Integer rating) {
}
