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

        String newTeamName = request.getName();
        if (teamRepository.existsByNameIgnoreCase(newTeamName)) {
            throw new RuntimeException("Команда с таким названием уже существует (регистронезависимо)");
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
        log.info("Пользователь {} пытается покинуть команду", currentUser.getUsername());
        
        // Находим команду пользователя
        TeamMember member = teamMemberRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Пользователь не состоит в команде"));
        
        Team team = member.getTeam();
        log.info("Пользователь {} состоит в команде: {} (ID: {})", currentUser.getUsername(), team.getName(), team.getId());
        
        // Проверяем, был ли пользователь лидером
        boolean wasLeader = team.getLeader().getId().equals(currentUser.getId());
        log.info("Пользователь {} был лидером: {}", currentUser.getUsername(), wasLeader);
        
        // Удаляем пользователя из команды
        teamMemberRepository.delete(member);
        log.info("Пользователь {} удален из таблицы TeamMember", currentUser.getUsername());
        
        // Если пользователь был лидером
        if (wasLeader) {
            // Если в команде есть другие участники, делаем первого лидером
            List<TeamMember> remainingMembers = teamMemberRepository.findByTeam(team);
            log.info("Осталось участников в команде: {}", remainingMembers.size());
            
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
        
        log.info("Пользователь {} успешно покинул команду: {}", currentUser.getUsername(), team.getName());
        
        // Проверяем, что пользователь действительно больше не состоит в команде
        boolean stillInTeam = teamMemberRepository.existsByUser(currentUser);
        log.info("Пользователь {} все еще состоит в команде: {}", currentUser.getUsername(), stillInTeam);
        
        return convertToTeamResponse(team);
    }
    
    public TeamResponse getCurrentUserTeam(User currentUser) {
        Team team = teamRepository.findByUserId(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Пользователь не состоит в команде"));
        
        return convertToTeamResponse(team);
    }
    
    public boolean isUserInTeam(User user) {
        return teamMemberRepository.existsByUser(user);
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
    
    @Transactional
    public TeamResponse kickMember(User currentUser, Long userIdToKick) {
        // Проверяем, что текущий пользователь состоит в команде
        TeamMember currentUserMember = teamMemberRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Пользователь не состоит в команде"));
        
        Team team = currentUserMember.getTeam();
        
        // Проверяем, что текущий пользователь является лидером команды
        if (!team.getLeader().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Только лидер команды может исключать участников");
        }
        
        // Проверяем, что пользователь, которого кикают, существует
        User userToKick = userRepository.findById(userIdToKick)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        
        // Проверяем, что пользователь, которого кикают, состоит в команде
        TeamMember memberToKick = teamMemberRepository.findByUser(userToKick)
                .orElseThrow(() -> new RuntimeException("Пользователь не состоит в команде"));
        
        // Проверяем, что пользователь, которого кикают, состоит в той же команде
        if (!memberToKick.getTeam().getId().equals(team.getId())) {
            throw new RuntimeException("Пользователь не состоит в вашей команде");
        }
        
        // Проверяем, что лидер не пытается кикнуть самого себя
        if (userToKick.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Лидер не может исключить самого себя");
        }
        
        // Удаляем пользователя из команды
        teamMemberRepository.delete(memberToKick);
        
        log.info("Пользователь {} исключен из команды {} лидером {}", 
                userToKick.getUsername(), team.getName(), currentUser.getUsername());
        
        // Если в команде остался только лидер, удаляем команду
        List<TeamMember> remainingMembers = teamMemberRepository.findByTeam(team);
        if (remainingMembers.isEmpty()) {
            teamRepository.delete(team);
            log.info("Команда {} удалена (не осталось участников)", team.getName());
            return null;
        }
        
        return convertToTeamResponse(team);
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
