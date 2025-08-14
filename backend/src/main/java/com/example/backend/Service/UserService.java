package com.example.backend.Service;

import com.example.backend.dto.CreateUserDto;
import com.example.backend.dto.LoginUserDto;
import com.example.backend.dto.ReturnUserDto;

public interface UserService {

    /**
     * Gets the user based on the bearer token given in the request header.
     *
     * @return the current user
     */
    ReturnUserDto getUser();

    /**
     * Creates a user in the database.
     *
     * @param createUserDto user to create
     * @return user token
     */
    String createUser(CreateUserDto createUserDto);

    /**
     * Login of a user.
     *
     * @param loginUserDto login data
     * @return user token if successful
     */
    String loginUser(LoginUserDto loginUserDto);
}
