package com.example.backend.REST;

import com.example.backend.Service.UserService;
import com.example.backend.dto.User.CreateUserDto;
import com.example.backend.dto.User.LoginUserDto;
import com.example.backend.dto.User.TokenDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserEndpoint {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<TokenDto> createUser(@Valid @RequestBody CreateUserDto createUserDto) {
        return new ResponseEntity<>(userService.createUser(createUserDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDto> login(@Valid @RequestBody LoginUserDto req) {
        return ResponseEntity.ok(userService.loginUser(req));
    }
}
