package com.qdeb.service;

import com.qdeb.dto.*;
import com.qdeb.entity.Team;
import com.qdeb.entity.TeamMember;
import com.qdeb.entity.User;
import com.qdeb.repository.TeamMemberRepository;
import com.qdeb.repository.TeamRepository;
import com.qdeb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeamService {
    
    private final TeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();
    
    @Transactional
    public TeamResponse createTeam(CreateTeamRequest request, User currentUser) {
        // Проверяем, что пользователь не состоит в команде
        if (teamMemberRepository.existsByUser(currentUser)) {
            throw new RuntimeException("Пользователь уже состоит в команде");
        }
        
        // Генерируем уникальный joinCode
        String joinCode = generateUniqueJoinCode();
        
        // Создаем команду
        Team team = new Team();
        team.setName(request.getName());
        team.setLeader(currentUser);
        team.setJoinCode(joinCode);
        
        Team savedTeam = teamRepository.save(team);
        
        // Добавляем лидера как участника команды
        TeamMember leaderMember = new TeamMember();
        leaderMember.setTeam(savedTeam);
        leaderMember.setUser(currentUser);
        teamMemberRepository.save(leaderMember);
        
        log.info("Создана команда: {} с лидером: {}", savedTeam.getName(), currentUser.getUsername());
        
        return convertToTeamResponse(savedTeam);
    }
    
    @Transactional
    public TeamResponse joinTeam(JoinTeamRequest request, User currentUser) {
        // Проверяем, что пользователь не состоит в команде
        if (teamMemberRepository.existsByUser(currentUser)) {
            throw new RuntimeException("Пользователь уже состоит в команде");
        }
        
        // Находим команду по коду
        Team team = teamRepository.findByJoinCode(request.getJoinCode())
                .orElseThrow(() -> new RuntimeException("Команда с таким кодом не найдена"));
        
        // Проверяем, что команда не полная
        if (team.isFull()) {
            throw new RuntimeException("Команда уже полная");
        }
        
        // Добавляем пользователя в команду
        TeamMember member = new TeamMember();
        member.setTeam(team);
        member.setUser(currentUser);
        teamMemberRepository.save(member);
        
        log.info("Пользователь {} вступил в команду: {}", currentUser.getUsername(), team.getName());
        
        return convertToTeamResponse(team);
    }
    
    @Transactional
    public TeamResponse leaveTeam(User currentUser) {
        // Находим команду пользователя
        TeamMember member = teamMemberRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Пользователь не состоит в команде"));
        
        Team team = member.getTeam();
        
        // Удаляем пользователя из команды
        teamMemberRepository.delete(member);
        
        // Если пользователь был лидером
        if (team.getLeader().getId().equals(currentUser.getId())) {
            // Если в команде есть другие участники, делаем первого лидером
            List<TeamMember> remainingMembers = teamMemberRepository.findByTeam(team);
            if (!remainingMembers.isEmpty()) {
                User newLeader = remainingMembers.get(0).getUser();
                team.setLeader(newLeader);
                teamRepository.save(team);
                log.info("Новый лидер команды {}: {}", team.getName(), newLeader.getUsername());
            } else {
                // Если никого не осталось, удаляем команду
                teamRepository.delete(team);
                log.info("Команда {} удалена (не осталось участников)", team.getName());
                return null;
            }
        }
        
        log.info("Пользователь {} покинул команду: {}", currentUser.getUsername(), team.getName());
        
        return convertToTeamResponse(team);
    }
    
    public TeamResponse getCurrentUserTeam(User currentUser) {
        Team team = teamRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не состоит в команде"));
        
        return convertToTeamResponse(team);
    }
    
    private String generateUniqueJoinCode() {
        String joinCode;
        do {
            joinCode = generateRandomCode();
        } while (teamRepository.findByJoinCode(joinCode).isPresent());
        
        return joinCode;
    }
    
    private String generateRandomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder(8);
        for (int i = 0; i < 8; i++) {
            code.append(chars.charAt(secureRandom.nextInt(chars.length())));
        }
        return code.toString();
    }
    
    private TeamResponse convertToTeamResponse(Team team) {
        return new TeamResponse(
                team.getId(),
                team.getName(),
                team.getLeader(),
                team.getJoinCode(),
                team.getCreatedAt(),
                team.getUpdatedAt(),
                team.getMemberCount(),
                team.isFull()
        );
    }
}
