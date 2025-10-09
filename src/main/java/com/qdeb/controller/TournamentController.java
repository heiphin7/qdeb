package com.qdeb.controller;

import com.qdeb.dto.CreateTournamentRequest;
import com.qdeb.entity.Tournament;
import com.qdeb.service.TournamentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
@Slf4j
public class TournamentController {
    
    private final TournamentService tournamentService;
    
    @PostMapping
    public ResponseEntity<?> createTournament(@RequestBody CreateTournamentRequest request) {
        try {
            log.info("Получен запрос на создание турнира: {}", request.getName());
            log.info("Детали запроса - Slug: {}, Организатор: {}, Дата: {}, Уровень: {}", 
                    request.getSlug(), request.getOrganizerName(), request.getDate(), request.getLevel());
            log.info("Количество полей регистрации: {}", 
                    request.getRegistrationFields() != null ? request.getRegistrationFields().size() : 0);
            log.info("Количество раундов: {}", 
                    request.getRounds() != null ? request.getRounds().size() : 0);
            
            Tournament tournament = tournamentService.createTournament(request);
            
            log.info("Турнир успешно создан с ID: {}", tournament.getId());
            
            return ResponseEntity.ok(tournament);
            
        } catch (Exception e) {
            log.error("Ошибка при создании турнира: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body("Ошибка при создании турнира: " + e.getMessage());
        }
    }
}
