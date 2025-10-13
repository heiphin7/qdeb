package com.qdeb.repository;

import com.qdeb.entity.ApplicationStatus;
import com.qdeb.entity.TournamentApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TournamentApplicationRepository extends JpaRepository<TournamentApplication, Long> {
    
    List<TournamentApplication> findByTournamentId(Long tournamentId);
    
    List<TournamentApplication> findByTournamentIdAndStatus(Long tournamentId, ApplicationStatus status);
    
    List<TournamentApplication> findByTeamId(Long teamId);
    
    List<TournamentApplication> findByTeamIdAndStatus(Long teamId, ApplicationStatus status);
    
    Optional<TournamentApplication> findByTournamentIdAndTeamId(Long tournamentId, Long teamId);
    
    List<TournamentApplication> findBySubmittedById(Long submittedById);
}
