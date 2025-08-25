package com.example.backend.dto.User;

import java.time.LocalDateTime;

public record ReturnUserDto(String username, String displayname, LocalDateTime createdAt, Integer rating) {
}