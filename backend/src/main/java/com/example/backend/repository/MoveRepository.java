package com.example.backend.repository;

import com.example.backend.entity.Move;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MoveRepository extends JpaRepository<Move, Long> {

    Optional<Move> findTopByGameIdOrderByPlyDesc(Long gameId);

    List<Move> findByGameIdAndPlyGreaterThanOrderByPlyAsc(Long gameId, int fromPly);
}