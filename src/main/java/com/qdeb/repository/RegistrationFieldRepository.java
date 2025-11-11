package com.qdeb.repository;

import com.qdeb.entity.RegistrationField;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationFieldRepository extends JpaRepository<RegistrationField, Long> {
    List<RegistrationField> findByTournamentId(Long tournamentId);
}
