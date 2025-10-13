package com.qdeb.controller;

import com.qdeb.dto.TournamentApplicationRequest;
import com.qdeb.dto.TournamentApplicationResponse;
import com.qdeb.dto.TournamentApplicationDetailResponse;
import com.qdeb.service.TournamentApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
@Slf4j
public class TournamentApplicationController {
    
    private final TournamentApplicationService applicationService;
    
    @PostMapping("/{tournamentId}/apply")
    public ResponseEntity<?> submitApplication(
            @PathVariable Long tournamentId,
            @RequestBody TournamentApplicationRequest request) {
        try {
            // Получаем текущего пользователя
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            log.info("Получен запрос на подачу заявки на турнир {} от пользователя {}", tournamentId, username);
            log.info("Детали заявки - Team ID: {}, Количество полей: {}", 
                    request.getTeamId(), 
                    request.getFields() != null ? request.getFields().size() : 0);
            
            TournamentApplicationResponse response = applicationService.submitApplication(tournamentId, request, username);
            
            log.info("Заявка успешно подана с ID: {}", response.getApplicationId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("Ошибка при подаче заявки на турнир {}: {}", tournamentId, e.getMessage(), e);
            
            if (e instanceof org.springframework.web.server.ResponseStatusException) {
                org.springframework.web.server.ResponseStatusException statusException = 
                        (org.springframework.web.server.ResponseStatusException) e;
                return ResponseEntity.status(statusException.getStatusCode())
                        .body(statusException.getReason());
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при подаче заявки: " + e.getMessage());
        }
    }
    
    @GetMapping("/{tournamentSlug}/applications")
    public ResponseEntity<?> getApplicationsByTournamentSlug(@PathVariable String tournamentSlug) {
        try {
            log.info("Получен запрос на получение заявок для турнира: {}", tournamentSlug);
            
            List<TournamentApplicationDetailResponse> applications = 
                    applicationService.getApplicationsByTournamentSlug(tournamentSlug);
            
            log.info("Успешно получено {} заявок для турнира {}", applications.size(), tournamentSlug);
            
            return ResponseEntity.ok(applications);
            
        } catch (Exception e) {
            log.error("Ошибка при получении заявок для турнира {}: {}", tournamentSlug, e.getMessage(), e);
            
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
}
