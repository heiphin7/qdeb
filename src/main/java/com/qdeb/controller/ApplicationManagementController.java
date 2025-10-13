package com.qdeb.controller;

import com.qdeb.dto.TournamentApplicationDetailResponse;
import com.qdeb.entity.User;
import com.qdeb.service.TournamentApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Slf4j
public class ApplicationManagementController {
    
    private final TournamentApplicationService applicationService;
    
    @PostMapping("/{applicationId}/accept")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> acceptApplication(@PathVariable Long applicationId) {
        try {
            User currentUser = getCurrentUser();
            log.info("Администратор {} принимает заявку {}", currentUser.getUsername(), applicationId);
            
            TournamentApplicationDetailResponse response = applicationService.acceptApplication(applicationId);
            
            log.info("Заявка {} успешно принята администратором {}", applicationId, currentUser.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Ошибка при принятии заявки {}: {}", applicationId, e.getMessage(), e);
            
            if (e instanceof org.springframework.web.server.ResponseStatusException) {
                org.springframework.web.server.ResponseStatusException statusException = 
                        (org.springframework.web.server.ResponseStatusException) e;
                return ResponseEntity.status(statusException.getStatusCode())
                        .body(statusException.getReason());
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при принятии заявки: " + e.getMessage());
        }
    }
    
    @PostMapping("/{applicationId}/reject")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> rejectApplication(@PathVariable Long applicationId) {
        try {
            User currentUser = getCurrentUser();
            log.info("Администратор {} отклоняет заявку {}", currentUser.getUsername(), applicationId);
            
            TournamentApplicationDetailResponse response = applicationService.rejectApplication(applicationId);
            
            log.info("Заявка {} успешно отклонена администратором {}", applicationId, currentUser.getUsername());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Ошибка при отклонении заявки {}: {}", applicationId, e.getMessage(), e);
            
            if (e instanceof org.springframework.web.server.ResponseStatusException) {
                org.springframework.web.server.ResponseStatusException statusException = 
                        (org.springframework.web.server.ResponseStatusException) e;
                return ResponseEntity.status(statusException.getStatusCode())
                        .body(statusException.getReason());
            }
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Ошибка при отклонении заявки: " + e.getMessage());
        }
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }
}
