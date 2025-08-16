package com.example.backend.repository;

import com.example.backend.entity.GameParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameParticipantRepository extends JpaRepository<GameParticipant, Long> {

    Optional<GameParticipant> findByGameIdAndUserUsername(long gameId, String userUsername);

    List<GameParticipant> findByGameId(Long gameId);
}