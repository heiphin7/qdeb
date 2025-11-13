package com.qdeb.controller;

import com.qdeb.dto.CreateTournamentRequest;
import com.qdeb.entity.Tournament;
import com.qdeb.service.TournamentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
@Slf4j
public class TournamentController {
    
    private final TournamentService tournamentService;
    private final ObjectMapper objectMapper;
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createTournament(
            @RequestParam("tournament") String tournamentJson,
            @RequestParam(value = "tournamentPicture", required = false) MultipartFile tournamentPicture) {
        try {
            // Парсим JSON
            CreateTournamentRequest request = objectMapper.readValue(tournamentJson, CreateTournamentRequest.class);
            
            log.info("Получен запрос на создание турнира: {} (только для ADMIN)", request.getName());
            log.info("Детали запроса - Slug: {}, Организатор: {}, Дата: {}, Уровень: {}", 
                    request.getSlug(), request.getOrganizerName(), request.getStartDate(), request.getLevel());
            log.info("Количество полей регистрации: {}", 
                    request.getRegistrationFields() != null ? request.getRegistrationFields().size() : 0);
            log.info("Фотография турнира: {}", tournamentPicture != null ? "загружена" : "не загружена");
            
            Tournament tournament = tournamentService.createTournament(request, tournamentPicture);
            
            log.info("Турнир успешно создан с ID: {}", tournament.getId());
            
            return ResponseEntity.ok(tournament);
            
        } catch (Exception e) {
            log.error("Ошибка при создании турнира: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body("Ошибка при создании турнира: " + e.getMessage());
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllTournaments() {
        try {
            log.info("Получен запрос на получение всех турниров");
            
            List<Tournament> tournaments = tournamentService.getAllTournaments();
            
            log.info("Успешно получено {} турниров", tournaments.size());
            
            return ResponseEntity.ok(tournaments);
            
        } catch (Exception e) {
            log.error("Ошибка при получении турниров: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body("Ошибка при получении турниров: " + e.getMessage());
        }
    }
    
    @GetMapping("/{slug}")
    public ResponseEntity<?> getTournamentBySlug(@PathVariable String slug) {
        try {
            log.info("Получен запрос на получение турнира по slug: {}", slug);
            
            Tournament tournament = tournamentService.getTournamentBySlug(slug);
            
            log.info("Турнир найден: {} (ID: {})", tournament.getName(), tournament.getId());
            
            return ResponseEntity.ok(tournament);
            
        } catch (RuntimeException e) {
            log.warn("Турнир с slug '{}' не найден: {}", slug, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Ошибка при получении турнира по slug '{}': {}", slug, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body("Ошибка при получении турнира: " + e.getMessage());
        }
    }
}
