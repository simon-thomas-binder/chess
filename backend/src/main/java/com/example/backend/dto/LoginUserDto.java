package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginUserDto(@NotBlank @Size(min=3,max=64) String username, @NotBlank @Size(min=8,max=24) String password) {
}
