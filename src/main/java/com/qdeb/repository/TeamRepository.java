package com.qdeb.repository;

import com.qdeb.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    
    Optional<Team> findByJoinCode(String joinCode);
    
    @Query("SELECT t FROM Team t JOIN t.members tm WHERE tm.user.id = :userId")
    Optional<Team> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Team t WHERE t.leader.id = :userId")
    Optional<Team> findByLeaderId(@Param("userId") Long userId);

    // Метод для регистронезависимой проверки существования команды по имени
    boolean existsByNameIgnoreCase(String name);
}
