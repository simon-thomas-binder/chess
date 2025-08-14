package com.example.backend.Service;

import com.example.backend.dto.CreateUserDto;
import com.example.backend.dto.LoginUserDto;
import com.example.backend.dto.ReturnUserDto;
import com.example.backend.entity.User;
import com.example.backend.exception.ValidationException;
import com.example.backend.repository.UserRepository;
import com.example.backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private static final int SALT_LENGTH = 32;
    private final String pepper;
    private final JwtUtil jwtUtil;

    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, @Value("${PEPPER:}") String pepper) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.pepper = pepper == null ? "" : pepper;
        if (this.pepper.isBlank()) {
            throw new IllegalStateException("PEPPER must be provided (set env or system property 'PEPPER')");
        }
    }

    @Override
    public ReturnUserDto getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return new ReturnUserDto(user.getUsername(), user.getDisplayname(), user.getCreatedAt(), user.getRating());
    }

    @Override
    public String createUser(CreateUserDto createUserDto) {

        validateUserCreation(createUserDto);

        User user = new User();
        String salt = generateSalt();
        user.setSalt(salt);
        user.setPassword(hashPassword(createUserDto.password(), salt));
        user.setUsername(createUserDto.username());
        user.setDisplayname(createUserDto.displayname());

        userRepository.save(user);

        return jwtUtil.generateToken(user.getUsername());
    }

    @Override
    public String loginUser(LoginUserDto loginUserDto) {
        User user = userRepository.findByUsername(loginUserDto.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials"));

        if (!checkPassword(loginUserDto.password(), user.getSalt(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "invalid credentials");
        }

        return jwtUtil.generateToken(user.getUsername());
    }

    private String hashPassword(String password, String salt) {
        return BCrypt.hashpw(salt + password + pepper, BCrypt.gensalt());
    }

    private boolean checkPassword(String password, String salt, String hashed) {
        return BCrypt.checkpw(salt + password + pepper, hashed);
    }

    private static String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    private void validateUserCreation(CreateUserDto user) {
        if (userRepository.existsByUsername(user.username())){
            throw new ValidationException("Username is already in use");
        }

        if (!(user.password().matches(".*[a-zA-Z].*") && user.password().matches(".*[0-9].*"))) {
            throw new ValidationException("Invalid password");
        }
    }
}
