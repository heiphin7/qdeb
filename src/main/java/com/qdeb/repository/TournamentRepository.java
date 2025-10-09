package com.qdeb.repository;

import com.qdeb.entity.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    Optional<Tournament> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
