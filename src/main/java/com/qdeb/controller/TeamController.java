package com.qdeb.controller;

import com.qdeb.dto.*;
import com.qdeb.entity.User;
import com.qdeb.service.TeamService;
import com.qdeb.service.TournamentApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
@Slf4j
public class TeamController {
    
    private final TeamService teamService;
    private final TournamentApplicationService applicationService;
    
    @PostMapping
    public ResponseEntity<?> createTeam(@Valid @RequestBody CreateTeamRequest request) {
        try {
            User currentUser = getCurrentUser();
            TeamResponse team = teamService.createTeam(request, currentUser);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при создании команды: " + e.getMessage());
        }
    }
    
    @PostMapping("/join")
    public ResponseEntity<?> joinTeam(@Valid @RequestBody JoinTeamRequest request) {
        try {
            User currentUser = getCurrentUser();
            TeamResponse team = teamService.joinTeam(request, currentUser);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при вступлении в команду: " + e.getMessage());
        }
    }
    
    @PostMapping("/leave")
    public ResponseEntity<?> leaveTeam() {
        try {
            User currentUser = getCurrentUser();
            TeamResponse team = teamService.leaveTeam(currentUser);
            
            if (team == null) {
                return ResponseEntity.ok("Команда удалена (не осталось участников)");
            } else {
                return ResponseEntity.ok(team);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при выходе из команды: " + e.getMessage());
        }
    }
    
    @GetMapping("/my")
    public ResponseEntity<?> getMyTeam() {
        try {
            User currentUser = getCurrentUser();
            TeamResponse team = teamService.getCurrentUserTeam(currentUser);
            return ResponseEntity.ok(team);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Ошибка при получении команды: " + e.getMessage());
        }
    }
    
    @GetMapping("/{teamId}/applications")
    public ResponseEntity<?> getTeamApplications(@PathVariable Long teamId) {
        try {
            log.info("Получен запрос на получение заявок для команды: {}", teamId);
            
            // Получаем текущего пользователя для проверки прав доступа
            User currentUser = getCurrentUser();
            
            // Проверяем, что пользователь является участником команды
            TeamResponse team = teamService.getCurrentUserTeam(currentUser);
            if (team == null || !team.getId().equals(teamId)) {
                log.warn("Пользователь {} пытается получить заявки команды {}, к которой не принадлежит", 
                        currentUser.getUsername(), teamId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("У вас нет доступа к заявкам этой команды");
            }
            
            List<TournamentApplicationDetailResponse> applications = 
                    applicationService.getApplicationsByTeamId(teamId);
            
            log.info("Успешно получено {} заявок для команды {}", applications.size(), teamId);
            
            return ResponseEntity.ok(applications);
            
        } catch (Exception e) {
            log.error("Ошибка при получении заявок для команды {}: {}", teamId, e.getMessage(), e);
            
            if (e instanceof org.springframework.web.server.ResponseStatusException) {
                org.springframework.web.server.ResponseStatusException statusException = 
                        (org.springframework.web.server.ResponseStatusException) e;
                return ResponseEntity.status(statusException.getStatusCode())
                        .body(statusException.getReason());
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при получении заявок: " + e.getMessage());
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
