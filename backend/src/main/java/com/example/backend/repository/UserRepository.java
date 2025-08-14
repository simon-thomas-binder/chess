package com.example.backend.repository;

import com.example.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Tests if user with given username exists.
     *
     * @param username name of the user
     * @return true if username already taken
     */
    boolean existsByUsername(String username);

    /**
     * Finds user by username.
     *
     * @param username name of the user
     * @return user object
     */
    Optional<User> findByUsername(String username);
}
