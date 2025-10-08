package com.qdeb.controller;

import com.qdeb.dto.*;
import com.qdeb.entity.User;
import com.qdeb.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    
    private final TeamService teamService;
    
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
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
