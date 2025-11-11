package com.qdeb.repository;

import com.qdeb.entity.Team;
import com.qdeb.entity.TeamMember;
import com.qdeb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember, Long> {
    
    Optional<TeamMember> findByUser(User user);
    
    void deleteByUser(User user);
    
    boolean existsByUser(User user);
    
    List<TeamMember> findByTeam(Team team);
}
