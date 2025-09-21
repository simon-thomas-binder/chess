package com.example.backend.Service;

import com.example.backend.dto.User.CreateUserDto;
import com.example.backend.dto.User.LoginUserDto;
import com.example.backend.dto.User.TokenDto;
import com.example.backend.entity.User;

public interface UserService {

    /**
     * Gets the user based on the bearer token given in the request header.
     *
     * @return the current user
     */
    User getUser();

    /**
     * Gets the user based on the provided username
     *
     * @param username of the user
     * @return the user if it exists
     */
    User getUser(String username);

    /**
     * Creates a user in the database.
     *
     * @param createUserDto user to create
     * @return user token
     */
    TokenDto createUser(CreateUserDto createUserDto);

    /**
     * Login of a user.
     *
     * @param loginUserDto login data
     * @return user token if successful
     */
    TokenDto loginUser(LoginUserDto loginUserDto);
}
